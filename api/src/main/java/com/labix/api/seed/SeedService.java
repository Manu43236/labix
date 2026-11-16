package com.labix.api.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labix.api.domain.*;
import com.labix.api.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final LabTestRepository testRepository;
    private final TestParameterRepository paramRepository;
    private final LabPackageRepository packageRepository;
    private final PackageTestRepository packageTestRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void seedTenant(String tenantId) {
        try {
            var resource = new ClassPathResource("seed/default-catalogue.json");
            SeedCatalogue seed = objectMapper.readValue(resource.getInputStream(), SeedCatalogue.class);

            Map<String, String> testIdByCode = new HashMap<>();

            for (SeedTest t : seed.getTests()) {
                LabTest test = new LabTest();
                test.setTenantId(tenantId);
                test.setCode(t.getCode());
                test.setName(t.getName());
                test.setCategory(t.getCategory());
                test.setSampleType(t.getSampleType());
                test.setTatHours(t.getTatHours());
                test.setActive(true);
                testRepository.save(test);
                testIdByCode.put(t.getCode(), test.getId());

                for (SeedParam p : t.getParameters()) {
                    TestParameter param = new TestParameter();
                    param.setTestId(test.getId());
                    param.setName(p.getName());
                    param.setUnit(p.getUnit());
                    param.setValueType(p.getValueType());
                    param.setDecimals(p.getDecimals());
                    param.setSortOrder(p.getSortOrder());
                    if (p.getChoices() != null && !p.getChoices().isEmpty()) {
                        param.setChoicesJson(objectMapper.writeValueAsString(p.getChoices()));
                    }
                    paramRepository.save(param);
                }
            }

            for (SeedPackage pkg : seed.getPackages()) {
                LabPackage labPkg = new LabPackage();
                labPkg.setTenantId(tenantId);
                labPkg.setName(pkg.getName());
                labPkg.setPricePaise(0);
                labPkg.setActive(true);
                packageRepository.save(labPkg);

                for (String testCode : pkg.getTests()) {
                    String testId = testIdByCode.get(testCode);
                    if (testId != null) {
                        PackageTest pt = new PackageTest();
                        pt.setPackageId(labPkg.getId());
                        pt.setTestId(testId);
                        packageTestRepository.save(pt);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed catalogue for tenant " + tenantId, e);
        }
    }

    // ── inner DTOs for JSON parsing ───────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SeedCatalogue {
        List<SeedTest> tests;
        List<SeedPackage> packages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SeedTest {
        String code, name, category, sampleType;
        Integer tatHours;
        List<SeedParam> parameters;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SeedParam {
        String name, unit, valueType;
        int decimals, sortOrder;
        List<String> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SeedPackage {
        String name;
        List<String> tests;
    }
}
