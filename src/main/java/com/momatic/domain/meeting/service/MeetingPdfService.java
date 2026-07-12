package com.momatic.domain.meeting.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** 회의록 상세 데이터를 PDF 파일로 변환하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class MeetingPdfService {

    private static final String FONT_PATH = "static/fonts/NanumGothic.ttf";

    private final MeetingService meetingService;

    /**
     * 접근 가능한 회의록 상세 데이터를 PDF 바이트 배열로 생성합니다.
     *
     * @param meetingId 회의 ID
     * @param requesterEmail 요청자 이메일
     * @return 생성된 PDF 바이트 배열
     */
    public byte[] generatePdf(Long meetingId, String requesterEmail) {
        MeetingService.MeetingDetail detail = meetingService.getAccessibleMeetingDetail(meetingId, requesterEmail);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            ConverterProperties properties = new ConverterProperties();
            properties.setFontProvider(createFontProvider());
            HtmlConverter.convertToPdf(buildHtml(detail), pdfDocument, properties);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * PDF 변환에 사용할 한글 폰트 제공자를 생성합니다.
     *
     * @return 폰트 제공자
     * @throws IOException 폰트 파일을 읽을 수 없는 경우
     */
    private FontProvider createFontProvider() throws IOException {
        FontProvider fontProvider = new FontProvider();
        fontProvider.addStandardPdfFonts();
        fontProvider.addFont(StandardFonts.HELVETICA);
        ClassPathResource fontResource = new ClassPathResource(FONT_PATH);
        try (InputStream inputStream = fontResource.getInputStream()) {
            fontProvider.addFont(inputStream.readAllBytes());
        }
        return fontProvider;
    }

    /**
     * 회의록 PDF 변환용 HTML 문자열을 생성합니다.
     *
     * @param detail 회의 상세 데이터
     * @return HTML 문자열
     */
    private String buildHtml(MeetingService.MeetingDetail detail) {
        Meeting meeting = detail.meeting();
        String summary = hasText(meeting.getSummary()) ? meeting.getSummary() : "요약 없음";
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8" />
              <style>
                body { font-family: 'NanumGothic', sans-serif; }
                table { width: 100%; border-collapse: collapse; }
                th, td { border: 1px solid #dddddd; padding: 8px; text-align: left; }
                th { background: #f5f5f5; }
              </style>
            </head>
            <body>
            """
                + "<h1>" + escapeHtml(meeting.getTitle()) + "</h1>"
                + "<h2>요약</h2>"
                + "<p>" + escapeHtml(summary) + "</p>"
                + "<h2>액션 아이템</h2>"
                + buildActionItemTable(detail.actionItems())
                + "</body></html>";
    }

    /**
     * 액션 아이템 목록 HTML 테이블을 생성합니다.
     *
     * @param actionItems 액션 아이템 목록
     * @return HTML 테이블 문자열
     */
    private String buildActionItemTable(List<ActionItem> actionItems) {
        StringBuilder builder = new StringBuilder("<table><thead><tr>")
                .append("<th>task</th><th>assignee</th><th>dueDate</th><th>status</th>")
                .append("</tr></thead><tbody>");
        for (ActionItem actionItem : actionItems) {
            builder.append("<tr>")
                    .append("<td>").append(escapeHtml(actionItem.getTask())).append("</td>")
                    .append("<td>").append(escapeHtml(actionItem.getAssignee())).append("</td>")
                    .append("<td>").append(actionItem.getDueDate() == null ? "" : actionItem.getDueDate()).append("</td>")
                    .append("<td>").append(actionItem.getStatus()).append("</td>")
                    .append("</tr>");
        }
        return builder.append("</tbody></table>").toString();
    }

    /**
     * 텍스트가 공백이 아닌 값을 포함하는지 확인합니다.
     *
     * @param text 확인할 텍스트
     * @return 텍스트 포함 여부
     */
    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    /**
     * HTML 특수 문자를 이스케이프합니다.
     *
     * @param value 원본 값
     * @return 이스케이프된 값
     */
    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}