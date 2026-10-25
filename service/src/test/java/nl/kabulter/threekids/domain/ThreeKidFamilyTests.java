package nl.kabulter.threekids.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThreeKidFamilyTests {

    private static final Long PARTNER = 2L;

    @Test
    void aPersonWithAPartnerAndThreeChildrenMatches() {
        assertThat(ThreeKidFamily.isMatchedBy(person(PARTNER, List.of(3L, 4L, 5L)))).isTrue();
    }

    @Test
    void aPersonWithoutAPartnerDoesNotMatch() {
        assertThat(ThreeKidFamily.isMatchedBy(person(null, List.of(3L, 4L, 5L)))).isFalse();
    }

    @Test
    void aPersonWithTwoChildrenDoesNotMatch() {
        assertThat(ThreeKidFamily.isMatchedBy(person(PARTNER, List.of(3L, 4L)))).isFalse();
    }

    private static Person person(Long partner, List<Long> children) {
        return new Person(1, null, null, null, null, partner, children);
    }
}
