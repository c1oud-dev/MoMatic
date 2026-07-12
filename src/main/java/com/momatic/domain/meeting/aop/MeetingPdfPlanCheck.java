package com.momatic.domain.meeting.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 회의록 PDF 내보내기 가능 플랜 여부를 검사하는 애노테이션입니다. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MeetingPdfPlanCheck {
}
