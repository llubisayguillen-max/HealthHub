package gui;

import java.awt.Font;

public class UiFonts {

    // Familia estándar para todo el sistema
    public static final String UI_FONT_FAMILY = "Segoe UI";
    // Titulares
    public static final Font H1_APP     = new Font(UI_FONT_FAMILY, Font.BOLD, 26); // Pantallas principales 
    public static final Font H2_SECTION = new Font(UI_FONT_FAMILY, Font.BOLD, 20); // Subtítulos
    public static final Font H3_LABEL   = new Font(UI_FONT_FAMILY, Font.BOLD, 16); // Etiquetas importantes
    // Body / texto 
    public static final Font BODY        = new Font(UI_FONT_FAMILY, Font.PLAIN, 14); // Texto normal
    public static final Font BODY_SMALL  = new Font(UI_FONT_FAMILY, Font.PLAIN, 12); // Texto pequeño
    public static final Font BODY_BOLD   = new Font(UI_FONT_FAMILY, Font.BOLD, 14);  // Texto normal en negrita
    // Cards
    public static final Font CARD_TITLE = new Font(UI_FONT_FAMILY, Font.BOLD, 17);
    public static final Font CARD_META  = new Font(UI_FONT_FAMILY, Font.PLAIN, 11);
    // Botones
    public static final Font BUTTON       = new Font(UI_FONT_FAMILY, Font.PLAIN, 13);
    public static final Font BUTTON_SMALL = new Font(UI_FONT_FAMILY, Font.PLAIN, 12);
    // Leyendas
    public static final Font CAPTION = new Font(UI_FONT_FAMILY, Font.PLAIN, 11); // texto auxiliar
}
