package fiskinfoo.no.sintef.fiskinfoo.Implementation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListChildObject;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListChildViewHolder;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListParentObject;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListParentViewHolder;
import fiskinfoo.no.sintef.fiskinfoo.R;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ExpandableRecyclerAdapter;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ParentObject;

public class MyPageExpandableListAdapter extends ExpandableRecyclerAdapter<ExpandableListParentViewHolder, ExpandableListChildViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    //private HorribleHackListener childrenOnClickListener = null;
    private LayoutInflater mInflater;
    private View.OnClickListener childrenOnClickListener = null;

    /**
     * Public primary constructor.
     *
     * @param context for inflating views
     * @param parentItemList the list of parent items to be displayed in the RecyclerView
     */
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
     * @param context for inflating views
     * @param parentItemList the list of parent items to be displayed in the RecyclerView
     * @param customClickableViewId the id of the view that triggers the expansion
     */
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
     * @param context for inflating views
     * @param parentItemList the list of parent items to be displayed in the RecyclerView
     * @param customClickableViewId the id of the view that triggers the expansion
     * @param animationDuration the duration (in ms) of the rotation animation
     */
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
     * @param position the position in the RecyclerView of the item
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
     * @param position the position in the RecyclerView of the item
     */
    @Override
    public void onBindChildViewHolder(ExpandableListChildViewHolder childViewHolder, int position, Object childObject) {
        ExpandableListChildObject customChildObject = (ExpandableListChildObject) childObject;
        if (this.childrenOnClickListener != null) {
            ((ExpandableListChildObject) childObject).setHorribleHack(childViewHolder.dataText);
            ((ExpandableListChildObject) childObject).setHorribleHackOnClickHacker(childrenOnClickListener);
        }
        childViewHolder.dataText.setText(customChildObject.getChildText());
    }

    public void setChildrenOnClickListener(View.OnClickListener childrenOnClickListener) {
        this.childrenOnClickListener = childrenOnClickListener;
    }
}