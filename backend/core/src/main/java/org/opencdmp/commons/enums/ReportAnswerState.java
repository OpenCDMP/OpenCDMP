package org.opencdmp.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Why a subject contributes no value to a question. Kept distinct because a question
 * hidden by a visibility rule was never asked, while an unanswered one was skipped — and
 * because an installation that has not run the plan field migration yet holds answers the
 * engine cannot read at all, which must never be reported as "not applicable".
 */
public enum ReportAnswerState {

    Answered((short) 0),
    NotAnswered((short) 1),
    NotApplicable((short) 2),
    NotMigrated((short) 3);

    private final Short value;

    ReportAnswerState(Short value) {
        this.value = value;
    }

    @JsonValue
    public Short getValue() {
        return this.value;
    }

    private static final Map<Short, ReportAnswerState> map = Arrays.stream(ReportAnswerState.values()).collect(Collectors.toMap(ReportAnswerState::getValue, x -> x));

    public static ReportAnswerState of(Short value) {
        return map.get(value);
    }
}
