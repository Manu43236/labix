package com.labix.api.catalogue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labix.api.catalogue.dto.*;
import com.labix.api.common.dto.PageResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.*;
import com.labix.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogueService {

    private final LabTestRepository testRepository;
    private final TestParameterRepository paramRepository;
    private final ReferenceRangeRepository rangeRepository;
    private final LabPackageRepository packageRepository;
    private final PackageTestRepository packageTestRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<TestResponse> listTests(String tenantId, String q, String category, Pageable pageable) {
        Page<LabTest> page;
        if (q != null && !q.isBlank()) {
            page = testRepository.search(tenantId, q.toLowerCase(), pageable);
        } else if (category != null && !category.isBlank()) {
            page = testRepository.findByTenantIdAndCategory(tenantId, category, pageable);
        } else {
            page = testRepository.findByTenantId(tenantId, pageable);
        }
        return PageResponse.of(page.map(t -> toTestResponse(t, false)));
    }

    @Transactional
    public TestResponse createTest(String tenantId, TestRequest req) {
        if (testRepository.findByTenantIdAndCode(tenantId, req.getCode()).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "CODE_TAKEN", "Test code already in use");
        }
        LabTest test = new LabTest();
        test.setTenantId(tenantId);
        test.setCode(req.getCode());
        test.setName(req.getName());
        test.setCategory(req.getCategory());
        test.setSampleType(req.getSampleType());
        test.setPricePaise(req.getPricePaise());
        test.setTatHours(req.getTatHours());
        test.setActive(true);
        testRepository.save(test);
        return toTestResponse(test, true);
    }

    @Transactional(readOnly = true)
    public TestResponse getTest(String tenantId, String id) {
        return toTestResponse(findTest(tenantId, id), true);
    }

    @Transactional
    public TestResponse updateTest(String tenantId, String id, TestRequest req) {
        LabTest test = findTest(tenantId, id);
        test.setName(req.getName());
        test.setCategory(req.getCategory());
        test.setSampleType(req.getSampleType());
        test.setPricePaise(req.getPricePaise());
        test.setTatHours(req.getTatHours());
        test.setActive(req.isActive());
        testRepository.save(test);
        return toTestResponse(test, true);
    }

    @Transactional
    public ParameterResponse addParameter(String tenantId, String testId, ParameterRequest req) {
        findTest(tenantId, testId);
        TestParameter p = buildParam(testId, req);
        paramRepository.save(p);
        return toParamResponse(p);
    }

    @Transactional
    public ParameterResponse updateParameter(String tenantId, String testId, String paramId, ParameterRequest req) {
        findTest(tenantId, testId);
        TestParameter p = paramRepository.findById(paramId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARAM_NOT_FOUND", "Parameter not found"));
        p.setName(req.getName());
        p.setUnit(req.getUnit());
        p.setValueType(req.getValueType());
        p.setDecimals(req.getDecimals());
        p.setSortOrder(req.getSortOrder());
        p.setChoicesJson(toChoicesJson(req.getChoices()));
        paramRepository.save(p);
        return toParamResponse(p);
    }

    @Transactional(readOnly = true)
    public List<RangeResponse> getRanges(String tenantId, String testId, String paramId) {
        findTest(tenantId, testId);
        return rangeRepository.findByParameterId(paramId).stream()
                .map(this::toRangeResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<RangeResponse> replaceRanges(String tenantId, String testId, String paramId, List<RangeRequest> ranges) {
        findTest(tenantId, testId);
        rangeRepository.deleteByParameterId(paramId);
        return ranges.stream().map(r -> {
            ReferenceRange rr = new ReferenceRange();
            rr.setParameterId(paramId);
            rr.setGender(r.getGender() != null ? r.getGender() : "ANY");
            rr.setAgeMinDays(r.getAgeMinDays());
            rr.setAgeMaxDays(r.getAgeMaxDays());
            rr.setLow(r.getLow());
            rr.setHigh(r.getHigh());
            rr.setCriticalLow(r.getCriticalLow());
            rr.setCriticalHigh(r.getCriticalHigh());
            rr.setRangeText(r.getRangeText());
            return toRangeResponse(rangeRepository.save(rr));
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<PackageResponse> listPackages(String tenantId, Pageable pageable) {
        return PageResponse.of(packageRepository.findByTenantId(tenantId, pageable).map(this::toPackageResponse));
    }

    @Transactional
    public PackageResponse createPackage(String tenantId, PackageRequest req) {
        LabPackage pkg = new LabPackage();
        pkg.setTenantId(tenantId);
        pkg.setName(req.getName());
        pkg.setPricePaise(req.getPricePaise());
        pkg.setActive(true);
        packageRepository.save(pkg);
        linkTests(pkg.getId(), req.getTestIds());
        return toPackageResponse(pkg);
    }

    @Transactional(readOnly = true)
    public PackageResponse getPackage(String tenantId, String id) {
        return toPackageResponse(findPackage(tenantId, id));
    }

    @Transactional
    public PackageResponse updatePackage(String tenantId, String id, PackageRequest req) {
        LabPackage pkg = findPackage(tenantId, id);
        pkg.setName(req.getName());
        pkg.setPricePaise(req.getPricePaise());
        pkg.setActive(req.isActive());
        packageRepository.save(pkg);
        packageTestRepository.deleteByPackageId(id);
        linkTests(id, req.getTestIds());
        return toPackageResponse(pkg);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private LabTest findTest(String tenantId, String id) {
        LabTest test = testRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "TEST_NOT_FOUND", "Test not found"));
        if (!test.getTenantId().equals(tenantId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "TEST_NOT_FOUND", "Test not found");
        }
        return test;
    }

    private LabPackage findPackage(String tenantId, String id) {
        LabPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND", "Package not found"));
        if (!pkg.getTenantId().equals(tenantId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND", "Package not found");
        }
        return pkg;
    }

    private void linkTests(String packageId, List<String> testIds) {
        if (testIds == null) return;
        for (String testId : testIds) {
            PackageTest pt = new PackageTest();
            pt.setPackageId(packageId);
            pt.setTestId(testId);
            packageTestRepository.save(pt);
        }
    }

    private TestParameter buildParam(String testId, ParameterRequest req) {
        TestParameter p = new TestParameter();
        p.setTestId(testId);
        p.setName(req.getName());
        p.setUnit(req.getUnit());
        p.setValueType(req.getValueType() != null ? req.getValueType() : "NUMBER");
        p.setDecimals(req.getDecimals());
        p.setSortOrder(req.getSortOrder());
        p.setChoicesJson(toChoicesJson(req.getChoices()));
        return p;
    }

    private String toChoicesJson(List<String> choices) {
        if (choices == null || choices.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(choices);
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_CHOICES", "Could not parse choices");
        }
    }

    private TestResponse toTestResponse(LabTest t, boolean includeParams) {
        TestResponse r = new TestResponse();
        r.setId(t.getId());
        r.setCode(t.getCode());
        r.setName(t.getName());
        r.setCategory(t.getCategory());
        r.setSampleType(t.getSampleType());
        r.setPricePaise(t.getPricePaise());
        r.setTatHours(t.getTatHours());
        r.setActive(t.isActive());
        if (includeParams) {
            r.setParameters(paramRepository.findByTestIdOrderBySortOrder(t.getId())
                    .stream().map(this::toParamResponse).collect(Collectors.toList()));
        }
        return r;
    }

    private ParameterResponse toParamResponse(TestParameter p) {
        ParameterResponse r = new ParameterResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setUnit(p.getUnit());
        r.setValueType(p.getValueType());
        r.setDecimals(p.getDecimals());
        r.setSortOrder(p.getSortOrder());
        r.setChoicesJson(p.getChoicesJson());
        return r;
    }

    private RangeResponse toRangeResponse(ReferenceRange rr) {
        RangeResponse r = new RangeResponse();
        r.setId(rr.getId());
        r.setGender(rr.getGender());
        r.setAgeMinDays(rr.getAgeMinDays());
        r.setAgeMaxDays(rr.getAgeMaxDays());
        r.setLow(rr.getLow());
        r.setHigh(rr.getHigh());
        r.setCriticalLow(rr.getCriticalLow());
        r.setCriticalHigh(rr.getCriticalHigh());
        r.setRangeText(rr.getRangeText());
        return r;
    }

    private PackageResponse toPackageResponse(LabPackage pkg) {
        PackageResponse r = new PackageResponse();
        r.setId(pkg.getId());
        r.setName(pkg.getName());
        r.setPricePaise(pkg.getPricePaise());
        r.setActive(pkg.isActive());
        r.setTestIds(packageTestRepository.findByPackageId(pkg.getId())
                .stream().map(PackageTest::getTestId).collect(Collectors.toList()));
        return r;
    }
}
