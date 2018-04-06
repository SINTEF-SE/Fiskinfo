package fiskinfoo.no.sintef.fiskinfoo.Implementation;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fiskinfoo.no.sintef.fiskinfoo.R;

public class MediumRecyclerCardViewAdapter extends RecyclerView.Adapter<MediumRecyclerCardViewAdapter.DataObjectHolder> {
    private static String LOG_TAG = "SmallRecycleCard";
    private boolean isColorful = false;
    private List<DefaultCardViewViewHolder> cardViewEntries;
    private static MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        private TextView title;
        private TextView subtitle;
        private TextView text;
        private Button positiveActionButton;
        private Button negativeActionButton;

        public DataObjectHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.material_medium_cardview_no_rich_media_title);
            subtitle = (TextView) itemView.findViewById(R.id.material_default_cardview_no_rich_media_subtitle);
            text = (TextView) itemView.findViewById(R.id.material_medium_cardview_no_rich_media_content);
            positiveActionButton = (Button) itemView.findViewById(R.id.material_medium_cardview_no_rich_media_delete_button);
            negativeActionButton = (Button) itemView.findViewById(R.id.material_medium_cardview_no_rich_media_negative_button);
            itemView.setOnClickListener(this);
            Log.i(LOG_TAG, "Finished creating the data object holder");
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public MediumRecyclerCardViewAdapter(List<DefaultCardViewViewHolder> myDataset, boolean isColorful) {
        cardViewEntries = myDataset;
        this.isColorful = isColorful;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        if(isColorful) {
            return new DataObjectHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.material_cardview_medium_color, parent, false));
        } else {
            return new DataObjectHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.material_medium_cardview, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        holder.title.setText(cardViewEntries.get(position).getTitle());
        if(cardViewEntries.get(position).getSubtitle().equals("")) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            if(holder.subtitle.getVisibility() == View.GONE) {
                holder.subtitle.setVisibility(View.VISIBLE);
            }
            holder.subtitle.setText(cardViewEntries.get(position).getSubtitle());
        }
        if(!(cardViewEntries.get(position).getContentText().equals(""))) {
            if(holder.text.getVisibility() != View.VISIBLE) {
                holder.text.setVisibility(View.VISIBLE);
            }
            if(cardViewEntries.get(position).shouldUseHtml()) {
                holder.text.setText(Html.fromHtml(cardViewEntries.get(position).getContentText()));
            } else {
                holder.text.setText(cardViewEntries.get(position).getContentText());
            }
        } else {
            holder.text.setVisibility(View.GONE);
        }

        if(cardViewEntries.get(position).getPositiveActionButtonListener() != null && !cardViewEntries.get(position).getPositiveActionButtonText().equals("")) {
            holder.positiveActionButton.setOnClickListener(cardViewEntries.get(position).getPositiveActionButtonListener());
            holder.positiveActionButton.setText(cardViewEntries.get(position).getPositiveActionButtonText());
        }
        if(cardViewEntries.get(position).getNegativeActionButtonText() != null) {
            if(cardViewEntries.get(position).getNegativeActionButtonListener() != null && !cardViewEntries.get(position).getNegativeActionButtonText().equals("")) {
                holder.negativeActionButton.setOnClickListener(cardViewEntries.get(position).getNegativeActionButtonListener());
                holder.negativeActionButton.setText(cardViewEntries.get(position).getNegativeActionButtonText());
                holder.negativeActionButton.setVisibility(View.VISIBLE);
            } else {
                holder.negativeActionButton.setVisibility(View.GONE);
            }
        }
    }

    public void addItem(DefaultCardViewViewHolder dataObj, int index) {
        cardViewEntries.add(index, dataObj);
        notifyItemInserted(index);
    }

    public DefaultCardViewViewHolder getItem(int index) {
        return cardViewEntries.get(index);
    }

    public void deleteItem(int index) {
        cardViewEntries.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return cardViewEntries.size();
    }

    public DefaultCardViewViewHolder getViewHolder(int index) {
        return cardViewEntries.get(index);
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }
}