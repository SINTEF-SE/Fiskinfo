package fiskinfoo.no.sintef.fiskinfoo.Implementation;

import android.view.View;

public abstract class DefaultCardViewViewHolder {
    public String getTitle() {
        return "";
    }

    public String getSubtitle() {
        return "";
    }

    public String getContentText() {
        return "";
    }

    public String getDescription() {
        return "";
    }

    public String getPositiveActionButtonText() {
        return "";
    }

    public String getNegativeActionButtonText() {
        return "";
    }

    public boolean shouldUseHtml() {
        return false;
    }

    public View.OnClickListener getPositiveActionButtonListener() {
        return null;
    }

    public View.OnClickListener getNegativeActionButtonListener() {
        return null;
    }
}
