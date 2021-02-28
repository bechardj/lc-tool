package us.jbec.lct.models;

import org.apache.commons.lang.StringUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;

public class LabeledImageCrop {

    private String label;
    private File source;
    private LocalDateTime created;
    private BufferedImage image;


    public LabeledImageCrop(String label, File source, BufferedImage image) {
        this.label = label;
        this.source = source;
        this.image = image;
        this.created = LocalDateTime.now();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public String getLabel() {
        // This is to avoid file system limitations.
        if(StringUtils.isAlpha(label) && StringUtils.isAllLowerCase(label)){
            return label + "_lower";
        } else if(StringUtils.isAlpha(label) && StringUtils.isAllUpperCase(label)){
            return label + "_upper";
        } else if(label.equals(".")) {
            return "period";
        } else if(label.equals("!")) {
            return "exclamation";
        } else if(label.equals("~")) {
            return "tilde";
        } else if(label.equals("`")) {
            return "backtick";
        } else if(label.equals("@")) {
            return "at";
        } else if(label.equals("#")) {
            return "pound";
        } else if(label.equals("<")) {
            return "left_angle_bracket";
        } else if(label.equals(">")) {
            return "right_angle_bracket";
        } else if(label.equals("$")) {
            return "dollar";
        } else if(label.equals("%")) {
            return "percent";
        } else if(label.equals("&")) {
            return "ampersand";
        } else if(label.equals("*")) {
            return "asterisk";
        } else if(label.equals("'")) {
            return "single_quote";
        } else if(label.equals("\"")) {
            return "double_quote";
        } else if(label.equals(" ")) {
            return "space";
        } else if(label.equals("\\")) {
            return "back_slash";
        } else if(label.equals("/")) {
            return "forward_slash";
        } else if(label.equals(":")) {
            return "colon";
        } else if(label.equals(";")) {
            return "semi_colon";
        } else if(label.equals("?")) {
            return "question";
        }else if(label.equals("{")) {
            return "curly_open";
        } else if(label.equals("}")) {
            return "curly_close";
        } else if(label.equals("+")) {
            return "plus";
        } else if(label.equals("|")) {
            return "pipe";
        } else if(label.equals("=")) {
            return "equals";
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
