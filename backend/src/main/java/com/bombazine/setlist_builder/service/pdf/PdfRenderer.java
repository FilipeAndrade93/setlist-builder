package com.bombazine.setlist_builder.service.pdf;

import com.bombazine.setlist_builder.dto.SetlistResponse;
import com.bombazine.setlist_builder.dto.SongResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfRenderer {

    private static final float MARGIN      = 50f;
    private static final float WIDTH       = PDRectangle.A4.getWidth();
    private static final float HEIGHT      = PDRectangle.A4.getHeight();
    private static final float SONG_SIZE   = 26f;
    private static final float DUR_SIZE    = 14f;
    private static final float HEADER_SIZE = 9f;
    // Space allocated to each song
    private static final float SONG_LEADING = SONG_SIZE + 4f + 12f;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("d MMMM yyyy");

    public byte[] render(SetlistResponse setlist) {
        try (PDDocument doc = new PDDocument()) {

            PDType1Font bold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            String headerLine = setlist.venueName()
                    + "  ·  " + setlist.eventDate().format(FMT)
                    + "  ·  " + setlist.formattedDuration();

            List<SongResponse> songs = setlist.songs();

            // Start first page
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = drawHeader(cs, headerLine, regular, true);

            for (int i = 0; i < songs.size(); i++) {
                // In case setlist overflows, start new page
                if (y - SONG_LEADING < MARGIN) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = drawHeader(cs, headerLine, regular, false);
                }

                // Song number + name
                cs.beginText();
                cs.setFont(bold, SONG_SIZE);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText((i + 1) + ".  " + songs.get(i).name());
                cs.endText();

                // Duration — right-aligned
                String dur = songs.get(i).formattedDuration();
                float durWidth = regular.getStringWidth(dur) / 1000 * DUR_SIZE;
                cs.beginText();
                cs.setFont(regular, DUR_SIZE);
                cs.newLineAtOffset(WIDTH - MARGIN - durWidth, y);
                cs.showText(dur);
                cs.endText();

                y -= SONG_LEADING;
            }

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Draws the header on a page and returns the y position ready for the
     * first song row. Page header only present on first page
     */
    private float drawHeader(PDPageContentStream cs, String headerLine,
                             PDType1Font regular, boolean firstPage)
            throws IOException {

        float y = HEIGHT - MARGIN;

        if (firstPage) {
            cs.beginText();
            cs.setFont(regular, HEADER_SIZE);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(headerLine);
            cs.endText();
            y -= (HEADER_SIZE + 4f) + 10f;

            // Thin divider
            cs.moveTo(MARGIN, y);
            cs.lineTo(WIDTH - MARGIN, y);
            cs.stroke();
            y -= 24f;
        } else {
            y -= 10f;
        }

        return y;
    }
}
