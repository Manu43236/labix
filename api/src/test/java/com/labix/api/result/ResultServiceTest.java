package com.labix.api.result;

import com.labix.api.domain.ReferenceRange;
import com.labix.api.domain.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultServiceTest {

    // ── computeFlag ──────────────────────────────────────────────────────────

    @Test
    void flagIsNullWhenValueIsNull() {
        assertThat(ResultService.computeFlag(null, new Result())).isNull();
    }

    @Test
    void flagIsNullWhenNoRangeDefined() {
        Result r = new Result();
        assertThat(ResultService.computeFlag(5.0, r)).isNull();
    }

    @Test
    void flagNormalWhenValueWithinRange() {
        Result r = resultWithRange(3.5, 5.0, null, null);
        assertThat(ResultService.computeFlag(4.0, r)).isEqualTo("NORMAL");
    }

    @Test
    void flagLowWhenBelowRangeLow() {
        Result r = resultWithRange(3.5, 5.0, null, null);
        assertThat(ResultService.computeFlag(2.0, r)).isEqualTo("LOW");
    }

    @Test
    void flagHighWhenAboveRangeHigh() {
        Result r = resultWithRange(3.5, 5.0, null, null);
        assertThat(ResultService.computeFlag(6.0, r)).isEqualTo("HIGH");
    }

    @Test
    void flagCriticalWhenBelowCriticalLow() {
        Result r = resultWithRange(3.5, 5.0, 2.0, 7.0);
        assertThat(ResultService.computeFlag(1.5, r)).isEqualTo("CRITICAL");
    }

    @Test
    void flagCriticalWhenAboveCriticalHigh() {
        Result r = resultWithRange(3.5, 5.0, 2.0, 7.0);
        assertThat(ResultService.computeFlag(8.0, r)).isEqualTo("CRITICAL");
    }

    @Test
    void criticalFlagTakesPrecedenceOverLow() {
        // value is below rangeLow AND below criticalLow
        Result r = resultWithRange(3.5, 5.0, 2.0, 7.0);
        assertThat(ResultService.computeFlag(1.9, r)).isEqualTo("CRITICAL");
    }

    @ParameterizedTest
    @CsvSource({"4.0,NORMAL", "3.5,NORMAL", "5.0,NORMAL", "3.4,LOW", "5.1,HIGH"})
    void flagBoundaryValues(double value, String expected) {
        Result r = resultWithRange(3.5, 5.0, null, null);
        assertThat(ResultService.computeFlag(value, r)).isEqualTo(expected);
    }

    // ── selectRange ──────────────────────────────────────────────────────────

    @Test
    void selectsExactGenderMatch() {
        List<ReferenceRange> ranges = List.of(
                range("M", null, null),
                range("F", null, null),
                range("ANY", null, null)
        );
        ReferenceRange r = ResultService.selectRange(ranges, "M", 365 * 30);
        assertThat(r.getGender()).isEqualTo("M");
    }

    @Test
    void fallsBackToAnyGenderWhenNoExactMatch() {
        List<ReferenceRange> ranges = List.of(
                range("F", null, null),
                range("ANY", null, null)
        );
        ReferenceRange r = ResultService.selectRange(ranges, "M", 365 * 30);
        assertThat(r.getGender()).isEqualTo("ANY");
    }

    @Test
    void selectsByAgeWindowWhenSpecified() {
        ReferenceRange adult = range("ANY", 18 * 365, 60 * 365);
        ReferenceRange child = range("ANY", 0, 17 * 365);
        List<ReferenceRange> ranges = List.of(adult, child);

        // 10-year-old
        ReferenceRange r = ResultService.selectRange(ranges, "M", 10 * 365);
        assertThat(r).isEqualTo(child);
    }

    @Test
    void prefersGenderMatchWithinAgeWindow() {
        ReferenceRange maleAdult = range("M", 18 * 365, 60 * 365);
        ReferenceRange anyAdult = range("ANY", 18 * 365, 60 * 365);
        List<ReferenceRange> ranges = List.of(anyAdult, maleAdult);

        ReferenceRange r = ResultService.selectRange(ranges, "M", 30 * 365);
        assertThat(r).isEqualTo(maleAdult);
    }

    @Test
    void fallsBackToAllRangesWhenNoAgeMatch() {
        // Ranges only cover 0–17; patient is 50 years old
        ReferenceRange childM = range("M", 0, 17 * 365);
        ReferenceRange childF = range("F", 0, 17 * 365);
        List<ReferenceRange> ranges = List.of(childM, childF);

        // Falls back to full list, picks gender match
        ReferenceRange r = ResultService.selectRange(ranges, "M", 50 * 365);
        assertThat(r).isEqualTo(childM);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Result resultWithRange(double low, double high, Double critLow, Double critHigh) {
        Result r = new Result();
        r.setRangeLow(low);
        r.setRangeHigh(high);
        r.setCriticalLow(critLow);
        r.setCriticalHigh(critHigh);
        return r;
    }

    private static ReferenceRange range(String gender, Integer ageMinDays, Integer ageMaxDays) {
        ReferenceRange r = new ReferenceRange();
        r.setGender(gender);
        r.setAgeMinDays(ageMinDays);
        r.setAgeMaxDays(ageMaxDays);
        return r;
    }
}
