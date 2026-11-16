package com.labix.api.result;

import com.labix.api.common.exception.AppException;
import com.labix.api.domain.*;
import com.labix.api.repository.*;
import com.labix.api.result.dto.ResultEntryItem;
import com.labix.api.result.dto.ResultResponse;
import com.labix.api.result.dto.UpdateRangeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ResultRepository resultRepository;
    private final VisitRepository visitRepository;
    private final VisitTestRepository visitTestRepository;
    private final PatientRepository patientRepository;
    private final TestParameterRepository parameterRepository;
    private final ReferenceRangeRepository rangeRepository;

    @Transactional
    public List<ResultResponse> enterResults(String tenantId, String visitId,
                                              List<ResultEntryItem> items, String enteredBy) {
        Visit visit = findVisit(tenantId, visitId);
        Patient patient = patientRepository.findById(visit.getPatientId()).orElseThrow();

        return items.stream().map(item -> {
            TestParameter param = parameterRepository.findById(item.getParameterId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                            "PARAM_NOT_FOUND", "Parameter not found: " + item.getParameterId()));

            VisitTest visitTest = visitTestRepository.findByVisitId(visitId).stream()
                    .filter(vt -> vt.getTestId().equals(param.getTestId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST,
                            "TEST_NOT_IN_VISIT", "Test not in this visit for parameter: " + item.getParameterId()));

            Result result = resultRepository
                    .findByVisitTestIdAndParameterId(visitTest.getId(), item.getParameterId())
                    .orElseGet(Result::new);

            result.setVisitTestId(visitTest.getId());
            result.setParameterId(item.getParameterId());
            result.setValueNumber(item.getValueNumber());
            result.setValueText(item.getValueText());
            result.setUnit(param.getUnit());
            result.setEnteredBy(enteredBy);
            result.setEnteredAt(LocalDateTime.now(ZoneOffset.UTC));

            // Auto-select reference range if not already range-edited
            if (!result.isRangeEdited()) {
                applyRange(result, item.getParameterId(), patient);
            }

            // Recompute flag
            result.setFlag(computeFlag(item.getValueNumber(), result));

            resultRepository.save(result);

            // Advance visit_test status to ENTERED on first result entry
            if ("PENDING".equals(visitTest.getStatus())) {
                visitTest.setStatus("ENTERED");
                visitTestRepository.save(visitTest);
            }

            return toResponse(result, param.getName());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResultResponse> listResults(String tenantId, String visitId) {
        findVisit(tenantId, visitId);
        return resultRepository.findByVisitId(visitId).stream()
                .map(r -> {
                    String paramName = parameterRepository.findById(r.getParameterId())
                            .map(TestParameter::getName).orElse("");
                    return toResponse(r, paramName);
                }).collect(Collectors.toList());
    }

    @Transactional
    public ResultResponse updateRange(String tenantId, String resultId, UpdateRangeRequest req) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "RESULT_NOT_FOUND", "Result not found"));

        // Verify the result belongs to this tenant via its visit
        VisitTest vt = visitTestRepository.findById(result.getVisitTestId()).orElseThrow();
        findVisit(tenantId, vt.getVisitId());

        result.setRangeLow(req.getRangeLow());
        result.setRangeHigh(req.getRangeHigh());
        result.setCriticalLow(req.getCriticalLow());
        result.setCriticalHigh(req.getCriticalHigh());
        result.setRangeText(req.getRangeText());
        result.setRangeEdited(true);
        result.setFlag(computeFlag(result.getValueNumber(), result));
        resultRepository.save(result);

        String paramName = parameterRepository.findById(result.getParameterId())
                .map(TestParameter::getName).orElse("");
        return toResponse(result, paramName);
    }

    // ── used by ReportService for snapshot building ───────────────────────────

    public List<Result> findResultsByVisitId(String visitId) {
        return resultRepository.findByVisitId(visitId);
    }

    // ── flag computation (package-private for unit testing) ───────────────────

    static String computeFlag(Double value, Result result) {
        if (value == null) return null;
        if (result.getCriticalLow() != null && value < result.getCriticalLow()) return "CRITICAL";
        if (result.getCriticalHigh() != null && value > result.getCriticalHigh()) return "CRITICAL";
        if (result.getRangeLow() != null && value < result.getRangeLow()) return "LOW";
        if (result.getRangeHigh() != null && value > result.getRangeHigh()) return "HIGH";
        if (result.getRangeLow() != null || result.getRangeHigh() != null) return "NORMAL";
        return null;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void applyRange(Result result, String parameterId, Patient patient) {
        List<ReferenceRange> ranges = rangeRepository.findByParameterId(parameterId);
        if (ranges.isEmpty()) return;

        int ageDays = computeAgeDays(patient);
        String gender = patient.getGender() != null ? patient.getGender() : "ANY";

        ReferenceRange best = selectRange(ranges, gender, ageDays);
        if (best == null) return;

        result.setRangeLow(best.getLow() != null ? best.getLow().doubleValue() : null);
        result.setRangeHigh(best.getHigh() != null ? best.getHigh().doubleValue() : null);
        result.setCriticalLow(best.getCriticalLow() != null ? best.getCriticalLow().doubleValue() : null);
        result.setCriticalHigh(best.getCriticalHigh() != null ? best.getCriticalHigh().doubleValue() : null);
        result.setRangeText(buildRangeText(best));
    }

    static ReferenceRange selectRange(List<ReferenceRange> ranges, String gender, int ageDays) {
        // Filter by age window (treat null as unbounded)
        List<ReferenceRange> ageMatches = ranges.stream()
                .filter(r -> (r.getAgeMinDays() == null || ageDays >= r.getAgeMinDays())
                          && (r.getAgeMaxDays() == null || ageDays <= r.getAgeMaxDays()))
                .toList();

        List<ReferenceRange> candidates = ageMatches.isEmpty() ? ranges : ageMatches;

        // Prefer exact gender match over ANY
        return candidates.stream()
                .filter(r -> gender.equals(r.getGender()))
                .findFirst()
                .or(() -> candidates.stream().filter(r -> "ANY".equals(r.getGender())).findFirst())
                .orElse(candidates.get(0));
    }

    private static int computeAgeDays(Patient patient) {
        if (patient.getDob() != null) {
            return (int) ChronoUnit.DAYS.between(patient.getDob(), java.time.LocalDate.now());
        }
        if (patient.getAgeYears() != null) {
            return patient.getAgeYears() * 365;
        }
        return 0;
    }

    private static String buildRangeText(ReferenceRange r) {
        if (r.getRangeText() != null && !r.getRangeText().isBlank()) return r.getRangeText();
        if (r.getLow() != null && r.getHigh() != null)
            return r.getLow().intValue() + " - " + r.getHigh().intValue();
        if (r.getLow() != null) return "≥ " + r.getLow().intValue();
        if (r.getHigh() != null) return "≤ " + r.getHigh().intValue();
        return "";
    }

    private Visit findVisit(String tenantId, String id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "Visit not found"));
        if (!tenantId.equals(visit.getTenantId()))
            throw new AppException(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "Visit not found");
        return visit;
    }

    private ResultResponse toResponse(Result r, String paramName) {
        ResultResponse resp = new ResultResponse();
        resp.setId(r.getId());
        resp.setVisitTestId(r.getVisitTestId());
        resp.setParameterId(r.getParameterId());
        resp.setParameterName(paramName);
        resp.setUnit(r.getUnit());
        resp.setValueNumber(r.getValueNumber());
        resp.setValueText(r.getValueText());
        resp.setRangeLow(r.getRangeLow());
        resp.setRangeHigh(r.getRangeHigh());
        resp.setCriticalLow(r.getCriticalLow());
        resp.setCriticalHigh(r.getCriticalHigh());
        resp.setRangeText(r.getRangeText());
        resp.setFlag(r.getFlag());
        resp.setRangeEdited(r.isRangeEdited());
        return resp;
    }
}
