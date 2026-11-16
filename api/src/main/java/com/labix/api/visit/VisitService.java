package com.labix.api.visit;

import com.labix.api.common.dto.PageResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.*;
import com.labix.api.repository.*;
import com.labix.api.visit.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final VisitTestRepository visitTestRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PaymentRepository paymentRepository;
    private final SampleRepository sampleRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final PatientRepository patientRepository;
    private final BranchRepository branchRepository;
    private final DoctorRepository doctorRepository;
    private final LabTestRepository labTestRepository;
    private final LabPackageRepository labPackageRepository;
    private final PackageTestRepository packageTestRepository;

    @Transactional
    public VisitResponse createVisit(String tenantId, String createdBy, CreateVisitRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .filter(p -> tenantId.equals(p.getTenantId()))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found"));

        Branch branch = branchRepository.findById(req.getBranchId())
                .filter(b -> tenantId.equals(b.getTenantId()))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "BRANCH_NOT_FOUND", "Branch not found"));

        Doctor doctor = null;
        if (req.getDoctorId() != null && !req.getDoctorId().isBlank()) {
            doctor = doctorRepository.findById(req.getDoctorId())
                    .filter(d -> tenantId.equals(d.getTenantId()))
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND", "Doctor not found"));
        }

        // Collect (test, packageId, price) for each test to book
        List<TestBooking> bookings = resolveBookings(tenantId, req);
        if (bookings.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "NO_TESTS", "At least one test or package is required");
        }

        // Create visit
        int visitNo = visitRepository.findMaxVisitNo(tenantId) + 1;
        Visit visit = new Visit();
        visit.setTenantId(tenantId);
        visit.setBranchId(branch.getId());
        visit.setPatientId(patient.getId());
        if (doctor != null) visit.setDoctorId(doctor.getId());
        visit.setVisitNo(visitNo);
        visit.setVisitType(req.getVisitType() != null ? req.getVisitType() : "WALK_IN");
        visit.setVisitDate(LocalDate.now(ZoneOffset.UTC));
        visit.setStatus("REGISTERED");
        visit.setCreatedBy(createdBy);
        visit.setUpdatedBy(createdBy);
        visitRepository.save(visit);

        // Create visit_tests
        List<VisitTest> visitTests = new ArrayList<>();
        for (TestBooking b : bookings) {
            VisitTest vt = new VisitTest();
            vt.setVisitId(visit.getId());
            vt.setTestId(b.test().getId());
            vt.setPackageId(b.packageId());
            vt.setPricePaise(b.pricePaise());
            vt.setCreatedBy(createdBy);
            vt.setUpdatedBy(createdBy);
            visitTestRepository.save(vt);
            visitTests.add(vt);
        }

        // Create bill
        Bill bill = buildBill(tenantId, visit.getId(), req, bookings, createdBy);

        // Create samples: one per distinct sample_type
        List<Sample> samples = buildSamples(tenantId, visit.getId(), bookings, createdBy);

        return toVisitResponse(visit, patient, branch, doctor, visitTests, bill, samples);
    }

    @Transactional(readOnly = true)
    public PageResponse<VisitResponse> listVisits(String tenantId, String status, String patientId, LocalDate date, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return PageResponse.of(visitRepository
                    .findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                    .map(this::toVisitResponseShallow));
        }
        if (patientId != null && !patientId.isBlank()) {
            return PageResponse.of(visitRepository
                    .findByTenantIdAndPatientIdAndDeletedAtIsNull(tenantId, patientId, pageable)
                    .map(this::toVisitResponseShallow));
        }
        if (date != null) {
            return PageResponse.of(visitRepository
                    .findByTenantIdAndVisitDateAndDeletedAtIsNull(tenantId, date, pageable)
                    .map(this::toVisitResponseShallow));
        }
        return PageResponse.of(visitRepository
                .findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::toVisitResponseShallow));
    }

    @Transactional(readOnly = true)
    public VisitResponse getVisit(String tenantId, String id) {
        Visit visit = findVisit(tenantId, id);
        Patient patient = patientRepository.findById(visit.getPatientId()).orElse(null);
        Branch branch = branchRepository.findById(visit.getBranchId()).orElse(null);
        Doctor doctor = visit.getDoctorId() != null
                ? doctorRepository.findById(visit.getDoctorId()).orElse(null) : null;
        List<VisitTest> tests = visitTestRepository.findByVisitId(id);
        Bill bill = billRepository.findByVisitId(id).orElse(null);
        List<Sample> samples = sampleRepository.findByVisitId(id);
        return toVisitResponse(visit, patient, branch, doctor, tests, bill, samples);
    }

    @Transactional
    public VisitResponse updateStatus(String tenantId, String id, String status) {
        Visit visit = findVisit(tenantId, id);
        visit.setStatus(status);
        visitRepository.save(visit);
        return getVisit(tenantId, id);
    }

    @Transactional(readOnly = true)
    public BillResponse getBill(String tenantId, String visitId) {
        findVisit(tenantId, visitId);
        Bill bill = billRepository.findByVisitId(visitId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "BILL_NOT_FOUND", "Bill not found"));
        return toBillResponse(bill);
    }

    @Transactional
    public BillResponse updateDiscount(String tenantId, String visitId, int discountPaise) {
        findVisit(tenantId, visitId);
        Bill bill = billRepository.findByVisitId(visitId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "BILL_NOT_FOUND", "Bill not found"));
        bill.setDiscountPaise(discountPaise);
        bill.setTotalPaise(Math.max(0, bill.getSubtotalPaise() - discountPaise));
        recalcBillStatus(bill);
        billRepository.save(bill);
        return toBillResponse(bill);
    }

    @Transactional
    public BillResponse recordPayment(String tenantId, String visitId, RecordPaymentRequest req) {
        findVisit(tenantId, visitId);
        Bill bill = billRepository.findByVisitId(visitId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "BILL_NOT_FOUND", "Bill not found"));

        Payment payment = new Payment();
        payment.setBillId(bill.getId());
        payment.setAmountPaise(req.getAmountPaise());
        payment.setMode(req.getMode() != null ? req.getMode() : "CASH");
        payment.setPaidAt(LocalDateTime.now(ZoneOffset.UTC));
        payment.setReference(req.getReference());
        paymentRepository.save(payment);

        bill.setPaidPaise(bill.getPaidPaise() + req.getAmountPaise());
        recalcBillStatus(bill);
        billRepository.save(bill);
        return toBillResponse(bill);
    }

    @Transactional(readOnly = true)
    public List<SampleResponse> listSamples(String tenantId, String visitId) {
        findVisit(tenantId, visitId);
        return sampleRepository.findByVisitId(visitId).stream()
                .map(this::toSampleResponse).collect(Collectors.toList());
    }

    @Transactional
    public SampleResponse updateSampleStatus(String tenantId, String sampleId, UpdateSampleStatusRequest req) {
        Sample sample = sampleRepository.findById(sampleId)
                .filter(s -> tenantId.equals(s.getTenantId()))
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "SAMPLE_NOT_FOUND", "Sample not found"));

        if ("REJECTED".equals(req.getStatus()) && (req.getRejectReason() == null || req.getRejectReason().isBlank())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "REJECT_REASON_REQUIRED", "Reject reason is required");
        }
        sample.setStatus(req.getStatus());
        if ("COLLECTED".equals(req.getStatus())) sample.setCollectedAt(LocalDateTime.now(ZoneOffset.UTC));
        if ("RECEIVED".equals(req.getStatus())) sample.setReceivedAt(LocalDateTime.now(ZoneOffset.UTC));
        if ("REJECTED".equals(req.getStatus())) sample.setRejectReason(req.getRejectReason());
        sampleRepository.save(sample);
        return toSampleResponse(sample);
    }

    @Transactional
    public CollectionRequestDto createCollectionRequest(String tenantId, String visitId, CollectionRequestDto req) {
        findVisit(tenantId, visitId);
        if (collectionRequestRepository.findByVisitId(visitId).isPresent()) {
            throw new AppException(HttpStatus.CONFLICT, "CR_EXISTS", "Collection request already exists for this visit");
        }
        CollectionRequest cr = new CollectionRequest();
        cr.setTenantId(tenantId);
        cr.setVisitId(visitId);
        cr.setAddress(req.getAddress());
        cr.setContactPhone(req.getContactPhone());
        cr.setSlotStart(req.getSlotStart());
        cr.setSlotEnd(req.getSlotEnd());
        cr.setNotes(req.getNotes());
        collectionRequestRepository.save(cr);
        return toCrDto(cr);
    }

    @Transactional(readOnly = true)
    public PageResponse<CollectionRequestDto> listCollectionRequests(String tenantId, String collectorId, Pageable pageable) {
        if (collectorId != null) {
            return PageResponse.of(collectionRequestRepository
                    .findByTenantIdAndCollectorId(tenantId, collectorId, pageable).map(this::toCrDto));
        }
        return PageResponse.of(collectionRequestRepository.findByTenantId(tenantId, pageable).map(this::toCrDto));
    }

    @Transactional
    public CollectionRequestDto assignCollector(String tenantId, String crId, String collectorUserId) {
        CollectionRequest cr = findCr(tenantId, crId);
        cr.setCollectorId(collectorUserId);
        cr.setStatus("ASSIGNED");
        collectionRequestRepository.save(cr);
        return toCrDto(cr);
    }

    @Transactional
    public CollectionRequestDto updateCrStatus(String tenantId, String crId, String status) {
        CollectionRequest cr = findCr(tenantId, crId);
        cr.setStatus(status);
        collectionRequestRepository.save(cr);
        return toCrDto(cr);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<TestBooking> resolveBookings(String tenantId, CreateVisitRequest req) {
        List<TestBooking> bookings = new ArrayList<>();
        if (req.getTestIds() != null) {
            for (String testId : req.getTestIds()) {
                LabTest test = labTestRepository.findById(testId)
                        .filter(t -> tenantId.equals(t.getTenantId()))
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "TEST_NOT_FOUND", "Test not found: " + testId));
                bookings.add(new TestBooking(test, null, test.getPricePaise()));
            }
        }
        if (req.getPackageIds() != null) {
            for (String pkgId : req.getPackageIds()) {
                LabPackage pkg = labPackageRepository.findById(pkgId)
                        .filter(p -> tenantId.equals(p.getTenantId()))
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND", "Package not found: " + pkgId));
                for (PackageTest pt : packageTestRepository.findByPackageId(pkgId)) {
                    labTestRepository.findById(pt.getTestId())
                            .ifPresent(t -> bookings.add(new TestBooking(t, pkgId, 0)));
                }
            }
        }
        return bookings;
    }

    private Bill buildBill(String tenantId, String visitId, CreateVisitRequest req,
                           List<TestBooking> bookings, String createdBy) {
        // Individual tests contribute their price; packages contribute package price, not per-test price
        int subtotal = bookings.stream()
                .filter(b -> b.packageId() == null)
                .mapToInt(TestBooking::pricePaise).sum();
        if (req.getPackageIds() != null) {
            for (String pkgId : req.getPackageIds()) {
                subtotal += labPackageRepository.findById(pkgId)
                        .map(LabPackage::getPricePaise).orElse(0);
            }
        }
        int discount = req.getDiscountPaise();
        int total = Math.max(0, subtotal - discount);

        int billNo = billRepository.findMaxBillNo(tenantId) + 1;
        Bill bill = new Bill();
        bill.setTenantId(tenantId);
        bill.setVisitId(visitId);
        bill.setBillNo(billNo);
        bill.setSubtotalPaise(subtotal);
        bill.setDiscountPaise(discount);
        bill.setTotalPaise(total);
        bill.setStatus(total == 0 ? "PAID" : "DUE");
        bill.setCreatedBy(createdBy);
        bill.setUpdatedBy(createdBy);
        billRepository.save(bill);

        // Bill items: one per individual test
        bookings.stream().filter(b -> b.packageId() == null).forEach(b -> {
            BillItem item = new BillItem();
            item.setBillId(bill.getId());
            item.setDescription(b.test().getName());
            item.setAmountPaise(b.pricePaise());
            item.setCreatedBy(createdBy);
            item.setUpdatedBy(createdBy);
            billItemRepository.save(item);
        });
        // Bill items: one per package
        if (req.getPackageIds() != null) {
            for (String pkgId : req.getPackageIds()) {
                labPackageRepository.findById(pkgId).ifPresent(pkg -> {
                    BillItem item = new BillItem();
                    item.setBillId(bill.getId());
                    item.setDescription(pkg.getName());
                    item.setAmountPaise(pkg.getPricePaise());
                    item.setCreatedBy(createdBy);
                    item.setUpdatedBy(createdBy);
                    billItemRepository.save(item);
                });
            }
        }
        return bill;
    }

    private List<Sample> buildSamples(String tenantId, String visitId,
                                       List<TestBooking> bookings, String createdBy) {
        List<String> distinctTypes = bookings.stream()
                .map(b -> b.test().getSampleType())
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .collect(Collectors.toList());

        int baseSampleNo = sampleRepository.findMaxSampleNo(tenantId);
        List<Sample> samples = new ArrayList<>();
        for (int i = 0; i < distinctTypes.size(); i++) {
            int sampleNo = baseSampleNo + i + 1;
            Sample s = new Sample();
            s.setTenantId(tenantId);
            s.setVisitId(visitId);
            s.setSampleNo(sampleNo);
            s.setBarcode(String.format("%07d", sampleNo));
            s.setSampleType(distinctTypes.get(i));
            s.setCreatedBy(createdBy);
            s.setUpdatedBy(createdBy);
            sampleRepository.save(s);
            samples.add(s);
        }
        return samples;
    }

    private Visit findVisit(String tenantId, String id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "Visit not found"));
        if (!tenantId.equals(visit.getTenantId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "Visit not found");
        }
        return visit;
    }

    private CollectionRequest findCr(String tenantId, String id) {
        CollectionRequest cr = collectionRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CR_NOT_FOUND", "Collection request not found"));
        if (!tenantId.equals(cr.getTenantId())) {
            throw new AppException(HttpStatus.NOT_FOUND, "CR_NOT_FOUND", "Collection request not found");
        }
        return cr;
    }

    private void recalcBillStatus(Bill bill) {
        if (bill.getPaidPaise() >= bill.getTotalPaise()) {
            bill.setStatus("PAID");
        } else if (bill.getPaidPaise() > 0) {
            bill.setStatus("PARTIAL");
        } else {
            bill.setStatus("DUE");
        }
    }

    private VisitResponse toVisitResponse(Visit v, Patient patient, Branch branch, Doctor doctor,
                                           List<VisitTest> tests, Bill bill, List<Sample> samples) {
        VisitResponse r = new VisitResponse();
        r.setId(v.getId());
        r.setVisitNo(v.getVisitNo());
        r.setVisitType(v.getVisitType());
        r.setVisitDate(v.getVisitDate());
        r.setStatus(v.getStatus());
        if (patient != null) { r.setPatientId(patient.getId()); r.setPatientName(patient.getName()); }
        if (branch != null) { r.setBranchId(branch.getId()); r.setBranchName(branch.getName()); }
        if (doctor != null) { r.setDoctorId(doctor.getId()); r.setDoctorName(doctor.getName()); }
        if (tests != null) {
            r.setTests(tests.stream().map(vt -> {
                VisitTestResponse tr = new VisitTestResponse();
                tr.setId(vt.getId());
                tr.setTestId(vt.getTestId());
                tr.setPackageId(vt.getPackageId());
                tr.setPricePaise(vt.getPricePaise());
                tr.setStatus(vt.getStatus());
                labTestRepository.findById(vt.getTestId()).ifPresent(t -> {
                    tr.setTestName(t.getName());
                    tr.setTestCode(t.getCode());
                });
                return tr;
            }).collect(Collectors.toList()));
        }
        if (bill != null) {
            BillSummary bs = new BillSummary();
            bs.setId(bill.getId()); bs.setBillNo(bill.getBillNo());
            bs.setSubtotalPaise(bill.getSubtotalPaise()); bs.setDiscountPaise(bill.getDiscountPaise());
            bs.setTotalPaise(bill.getTotalPaise()); bs.setPaidPaise(bill.getPaidPaise());
            bs.setStatus(bill.getStatus());
            r.setBill(bs);
        }
        if (samples != null) r.setSamples(samples.stream().map(this::toSampleResponse).collect(Collectors.toList()));
        return r;
    }

    private VisitResponse toVisitResponseShallow(Visit v) {
        VisitResponse r = new VisitResponse();
        r.setId(v.getId()); r.setVisitNo(v.getVisitNo());
        r.setVisitType(v.getVisitType()); r.setVisitDate(v.getVisitDate());
        r.setStatus(v.getStatus()); r.setPatientId(v.getPatientId());
        r.setBranchId(v.getBranchId()); r.setDoctorId(v.getDoctorId());
        patientRepository.findById(v.getPatientId()).ifPresent(p -> r.setPatientName(p.getName()));
        return r;
    }

    private BillResponse toBillResponse(Bill bill) {
        BillResponse r = new BillResponse();
        r.setId(bill.getId()); r.setBillNo(bill.getBillNo());
        r.setSubtotalPaise(bill.getSubtotalPaise()); r.setDiscountPaise(bill.getDiscountPaise());
        r.setTaxPaise(bill.getTaxPaise()); r.setTotalPaise(bill.getTotalPaise());
        r.setPaidPaise(bill.getPaidPaise()); r.setStatus(bill.getStatus());
        r.setItems(billItemRepository.findByBillId(bill.getId()).stream().map(i -> {
            BillResponse.BillItemResponse ir = new BillResponse.BillItemResponse();
            ir.setId(i.getId()); ir.setDescription(i.getDescription()); ir.setAmountPaise(i.getAmountPaise());
            return ir;
        }).collect(Collectors.toList()));
        r.setPayments(paymentRepository.findByBillId(bill.getId()).stream().map(p -> {
            BillResponse.PaymentResponse pr = new BillResponse.PaymentResponse();
            pr.setId(p.getId()); pr.setAmountPaise(p.getAmountPaise());
            pr.setMode(p.getMode()); pr.setPaidAt(p.getPaidAt().toString());
            pr.setReference(p.getReference());
            return pr;
        }).collect(Collectors.toList()));
        return r;
    }

    private SampleResponse toSampleResponse(Sample s) {
        SampleResponse r = new SampleResponse();
        r.setId(s.getId()); r.setSampleNo(s.getSampleNo());
        r.setBarcode(s.getBarcode()); r.setSampleType(s.getSampleType());
        r.setStatus(s.getStatus()); r.setRejectReason(s.getRejectReason());
        return r;
    }

    private CollectionRequestDto toCrDto(CollectionRequest cr) {
        CollectionRequestDto d = new CollectionRequestDto();
        d.setId(cr.getId()); d.setVisitId(cr.getVisitId());
        d.setAddress(cr.getAddress()); d.setContactPhone(cr.getContactPhone());
        d.setSlotStart(cr.getSlotStart()); d.setSlotEnd(cr.getSlotEnd());
        d.setNotes(cr.getNotes()); d.setCollectorId(cr.getCollectorId());
        d.setStatus(cr.getStatus());
        return d;
    }

    private record TestBooking(LabTest test, String packageId, int pricePaise) {}
}
