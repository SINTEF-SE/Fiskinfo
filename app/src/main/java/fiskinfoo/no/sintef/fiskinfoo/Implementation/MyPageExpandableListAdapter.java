package fiskinfoo.no.sintef.fiskinfoo.Implementation;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListChildObject;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListChildViewHolder;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListParentObject;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListParentViewHolder;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionExpandableListChildObject;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.ApiErrorType;
import fiskinfoo.no.sintef.fiskinfoo.R;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ExpandableRecyclerAdapter;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ParentObject;

public class MyPageExpandableListAdapter extends ExpandableRecyclerAdapter<ExpandableListParentViewHolder, ExpandableListChildViewHolder> {
    private LayoutInflater mInflater;
    private View.OnClickListener childrenOnClickListener = null;

    /**
     * Public primary constructor.
     *
     * @param context        for inflating views
     * @param parentItemList the list of parent items to be displayed in the RecyclerView
     */
    @SuppressWarnings("unused")
    public MyPageExpandableListAdapter(Context context, List<ParentObject> parentItemList) {
        super(context, parentItemList);
        mInflater = LayoutInflater.from(context);
    }

    public MyPageExpandableListAdapter(Context context, List<ParentObject> parentItemList, View.OnClickListener childrenOnClickListener) {
        super(context, parentItemList);
        mInflater = LayoutInflater.from(context);
        this.childrenOnClickListener = childrenOnClickListener;
    }

    /**
     * Public secondary constructor. This constructor adds the ability to add a custom triggering
     * view when the adapter is created without having to set it later. This is here for demo
     * purposes.
     *
     * @param context               for inflating views
     * @param parentItemList        the list of parent items to be displayed in the RecyclerView
     * @param customClickableViewId the id of the view that triggers the expansion
     */
    @SuppressWarnings("unused")
    public MyPageExpandableListAdapter(Context context, List<ParentObject> parentItemList,
                                       int customClickableViewId) {
        super(context, parentItemList, customClickableViewId);
        mInflater = LayoutInflater.from(context);
    }

    /**
     * Public secondary constructor. This constructor adds the ability to add a custom triggering
     * view and a custom animation duration when the adapter is created without having to set them
     * later. This is here for demo purposes.
     *
     * @param context               for inflating views
     * @param parentItemList        the list of parent items to be displayed in the RecyclerView
     * @param customClickableViewId the id of the view that triggers the expansion
     * @param animationDuration     the duration (in ms) of the rotation animation
     */
    @SuppressWarnings("unused")
    public MyPageExpandableListAdapter(Context context, List<ParentObject> parentItemList,
                                       int customClickableViewId, long animationDuration) {
        super(context, parentItemList, customClickableViewId, animationDuration);
        mInflater = LayoutInflater.from(context);
    }

    /**
     * OnCreateViewHolder implementation for parent items. The desired ParentViewHolder should
     * be inflated here
     *
     * @param parent for inflating the View
     * @return the user's custom parent ViewHolder that must extend ParentViewHolder
     */
    @Override
    public ExpandableListParentViewHolder onCreateParentViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.recycler_item_layout_parent, parent, false);
        return new ExpandableListParentViewHolder(view);
    }

    /**
     * OnCreateViewHolder implementation for child items. The desired ChildViewHolder should
     * be inflated here
     *
     * @param parent for inflating the View
     * @return the user's custom parent ViewHolder that must extend ParentViewHolder
     */
    @Override
    public ExpandableListChildViewHolder onCreateChildViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.recycler_item_layout_child, parent, false);
        return new ExpandableListChildViewHolder(view);
    }


    /**
     * OnBindViewHolder implementation for parent items. Any data or view modifications of the
     * parent view should be performed here.
     *
     * @param parentViewHolder the ViewHolder of the parent item created in OnCreateParentViewHolder
     * @param position         the position in the RecyclerView of the item
     */
    @Override
    public void onBindParentViewHolder(ExpandableListParentViewHolder parentViewHolder, int position, Object parentObject) {
        ExpandableListParentObject customParentObject = (ExpandableListParentObject) parentObject;
        parentViewHolder.dataText.setText(customParentObject.getParentText());
        parentViewHolder.logoContainer.setImageResource(customParentObject.getResourcePathToImageResource());
    }

    /**
     * OnBindViewHolder implementation for child items. Any data or view modifications of the
     * child view should be performed here.
     *
     * @param childViewHolder the ViewHolder of the child item created in OnCreateChildViewHolder
     * @param position        the position in the RecyclerView of the item
     */
    @Override
    public void onBindChildViewHolder(ExpandableListChildViewHolder childViewHolder, int position, Object childObject) {
        switch (((ExpandableListChildObject) childObject).getObjectType()) {
            case EXPANDABLE_LIST_CHILD_OBJECT:
                bindChildViewHolder(childViewHolder, childObject);
                break;
            case SUBSCRIPTION_EXPANDABLE_LIST_CHILD_OBJECT:
                bindSubscriptionChildViewHolder(childViewHolder, childObject);
                break;
            default:
                Log.e("Unknown type", "Type not supported");
        }

    }

    private void bindChildViewHolder(ExpandableListChildViewHolder childViewHolder, Object childObject) {
        ExpandableListChildObject customChildObject = (ExpandableListChildObject) childObject;

        if (this.childrenOnClickListener != null) {
            customChildObject.setTitleTextView(childViewHolder.dataText);
            customChildObject.setOnClickListener(childrenOnClickListener);
        }
        childViewHolder.dataText.setText(customChildObject.getTitleText());
    }

    private void bindSubscriptionChildViewHolder(ExpandableListChildViewHolder childViewHolder, Object childObject) {
        Context context = childViewHolder.lastUpdatedTextView.getContext();

        if(((SubscriptionExpandableListChildObject) childObject).getErrorType() == ApiErrorType.WARNING) {
            childViewHolder.notificationImageView.setVisibility(View.VISIBLE);
            childViewHolder.notificationImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_error_outline_black_24dp));
            childViewHolder.notificationImageView.setOnClickListener(((SubscriptionExpandableListChildObject) childObject).getErrorNotificationOnClickListener());

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childViewHolder.notificationImageView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_icon_black_active_tint_color));
            }
        } else if(((SubscriptionExpandableListChildObject) childObject).getErrorType() == ApiErrorType.ERROR) {
            childViewHolder.notificationImageView.setVisibility(View.VISIBLE);
            childViewHolder.notificationImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_warning_black_24dp));
            childViewHolder.notificationImageView.setOnClickListener(((SubscriptionExpandableListChildObject) childObject).getErrorNotificationOnClickListener());

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childViewHolder.notificationImageView.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_icon_black_active_tint_color));
            }
        } else {
            childViewHolder.notificationImageView.setVisibility(View.GONE);
        }

        childViewHolder.dataText.setText(((SubscriptionExpandableListChildObject) childObject).getTitleText());
        childViewHolder.dataText.setOnClickListener(childrenOnClickListener);

        childViewHolder.lastUpdatedTextView.setText(((SubscriptionExpandableListChildObject) childObject).getLastUpdatedText());
        childViewHolder.lastUpdatedTextView.setGravity(Gravity.CENTER);
        childViewHolder.subscribedCheckBox.setChecked(((SubscriptionExpandableListChildObject) childObject).getIsSubscribed());


        ((ViewGroup) childViewHolder.dataText.getParent()).setTag(((SubscriptionExpandableListChildObject) childObject).getTitleText());
        childViewHolder.dataText.setText(((SubscriptionExpandableListChildObject) childObject).getTitleText());

        childViewHolder.downloadButton.setOnClickListener(((SubscriptionExpandableListChildObject) childObject).getDownloadButtonOnClickListener());
        childViewHolder.subscribedCheckBox.setOnClickListener(((SubscriptionExpandableListChildObject) childObject).getSubscribeSwitchOnClickListener());
        childViewHolder.subscribedCheckBox.setChecked(((SubscriptionExpandableListChildObject) childObject).getIsSubscribed());

        if(!(new FiskInfoUtility().isNetworkAvailable(context))) {
            childViewHolder.subscribedCheckBox.setEnabled(false);
            childViewHolder.downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(view.getContext())
                            .setIcon(R.drawable.ic_info_outline_black_24dp)
                            .setTitle(view.getContext().getString(R.string.download_title))
                            .setMessage(view.getContext().getString(R.string.error_cannot_download_without_internet_access))
                            .setPositiveButton(view.getContext().getString(R.string.ok), null)
                            .show();
                }
            });

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childViewHolder.downloadButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_icon_black_active_tint_color));
            }
        } else if(!((SubscriptionExpandableListChildObject) childObject).getAuthorized() && !childViewHolder.dataText.getText().toString().equals(context.getString(R.string.fishing_facility_name))) {
            childViewHolder.subscribedCheckBox.setEnabled(false);
            childViewHolder.downloadButton.setEnabled(false);
//            childViewHolder.downloadButton.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_info_outline_black_24dp));

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childViewHolder.downloadButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_icon_black_disabled_tint_color));
            }
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                childViewHolder.downloadButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.material_icon_black_active_tint_color));
            }
        }
    }


}