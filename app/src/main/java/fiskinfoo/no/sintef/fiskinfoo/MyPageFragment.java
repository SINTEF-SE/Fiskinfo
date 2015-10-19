/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fiskinfoo.no.sintef.fiskinfoo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListParentObject;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionEntry;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionExpandableListChildObject;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.ApiErrorType;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.BarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.IBarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.Authorization;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.PropertyDescription;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.Subscription;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskInfoUtility;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.MyPageExpandableListAdapter;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.User;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityOnClickListeners;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ExpandCollapseListener;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ParentObject;

public class MyPageFragment extends Fragment implements ExpandCollapseListener {
    FragmentActivity listener;
    public static final String TAG = "MyPageFragment";
    private User user;
    private MyPageExpandableListAdapter myPageExpandableListAdapter;
    private ExpandableListAdapterChildOnClickListener childOnClickListener;
    private RecyclerView mCRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private UtilityOnClickListeners onClickListenerInterface;
    private FiskInfoUtility fiskInfoUtility;
    private ExpandCollapseListener expandCollapseListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = (FragmentActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");
        childOnClickListener = new ExpandableListAdapterChildOnClickListener();
        onClickListenerInterface = new UtilityOnClickListeners();
        fiskInfoUtility = new FiskInfoUtility();
    }


    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_my_page, parent, false);
        ArrayList<ParentObject> data;
        expandCollapseListener = this;
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.my_page_swipe_refresh_layout);

        if(fiskInfoUtility.isNetworkAvailable(getActivity())) {
            data = fetchMyPage();
            myPageExpandableListAdapter = new MyPageExpandableListAdapter(this.getActivity(), data, childOnClickListener);
            myPageExpandableListAdapter.addExpandCollapseListener(expandCollapseListener);
            myPageExpandableListAdapter.onRestoreInstanceState(savedInstanceState);
        } else {
            data = fetchMyPageCached();
            myPageExpandableListAdapter = new MyPageExpandableListAdapter(this.getActivity(), data, childOnClickListener);
            myPageExpandableListAdapter.addExpandCollapseListener(expandCollapseListener);
            myPageExpandableListAdapter.onRestoreInstanceState(savedInstanceState);
        }

        mCRecyclerView = (RecyclerView) v.findViewById(R.id.recycle_view);
        mCRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mCRecyclerView.setAdapter(myPageExpandableListAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(TAG, savedInstanceState.toString());

            myPageExpandableListAdapter.onRestoreInstanceState(savedInstanceState);
            Parcelable manager = savedInstanceState.getParcelable("recycleLayout");
            LinearLayoutManager manager1 = new LinearLayoutManager(this.getActivity());
            if(manager != null) {
                mCRecyclerView.setLayoutManager(manager1);
                manager1.onRestoreInstanceState(manager);
            }
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: Will not work if token has expired, should look into fixing this in general
                boolean networkAvailable = fiskInfoUtility.isNetworkAvailable(getActivity());

                if (networkAvailable) {
                    ArrayList<ParentObject> data = fetchMyPage();

                    myPageExpandableListAdapter = new MyPageExpandableListAdapter(getActivity(), data, childOnClickListener);
                    myPageExpandableListAdapter.addExpandCollapseListener(expandCollapseListener);
                    mCRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mCRecyclerView.setAdapter(myPageExpandableListAdapter);

                    ((MainActivity) getActivity()).toggleNetworkErrorTextView(fiskInfoUtility.isNetworkAvailable(getActivity()));
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    ArrayList<ParentObject> data = fetchMyPageCached();

                    myPageExpandableListAdapter = new MyPageExpandableListAdapter(getActivity(), data, childOnClickListener);
                    myPageExpandableListAdapter.addExpandCollapseListener(expandCollapseListener);
                    mCRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mCRecyclerView.setAdapter(myPageExpandableListAdapter);

                    ((MainActivity) getActivity()).toggleNetworkErrorTextView(fiskInfoUtility.isNetworkAvailable(getActivity()));
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        ((MainActivity)getActivity()).toggleNetworkErrorTextView(fiskInfoUtility.isNetworkAvailable(getActivity()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            Log.d(TAG, outState.toString());
        }

        if(mCRecyclerView != null && mCRecyclerView.getAdapter() != null) {
            ((MyPageExpandableListAdapter) mCRecyclerView.getAdapter()).onSaveInstanceState(outState);
            Parcelable layoutState = mCRecyclerView.getLayoutManager().onSaveInstanceState();
            if(outState != null) {
                outState.putParcelable("recycleLayout", layoutState);
            }
        }

    }

    public ArrayList<ParentObject> fetchMyPage() {
        BarentswatchApi barentswatchApi = new BarentswatchApi();
        barentswatchApi.setAccesToken(user.getToken());
        final IBarentswatchApi api = barentswatchApi.getApi();
        ArrayList<ParentObject> parentObjectList = new ArrayList<>();
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            List<PropertyDescription> availableSubscriptions = api.getSubscribable();
            List<Subscription> currentSubscriptions = api.getSubscriptions();
            List<Authorization> authorizations = api.getAuthorization();
            Map<Integer, Boolean> authMap = new HashMap<>();
            Map<String, PropertyDescription> availableSubscriptionsMap = new HashMap<>();
            Map<String, Subscription> activeSubscriptionsMap = new HashMap<>();
            ArrayList<Object> availableSubscriptionObjectsList = new ArrayList<>();

            for(Authorization auth : authorizations) {
                authMap.put(auth.Id, auth.HasAccess);
            }

            for(Subscription subscription : currentSubscriptions) {
                activeSubscriptionsMap.put(subscription.GeoDataServiceName, subscription);
            }

            for(PropertyDescription subscribable : availableSubscriptions) {
                availableSubscriptionsMap.put(subscribable.ApiName, subscribable);
                SubscriptionEntry currentEntry = user.getSubscriptionCacheEntry(subscribable.ApiName);

                if(currentEntry == null) {
                    SubscriptionEntry entry = activeSubscriptionsMap.get(subscribable.ApiName) == null ? new SubscriptionEntry(subscribable, authMap.get(subscribable.Id)) :
                            new SubscriptionEntry(subscribable, activeSubscriptionsMap.get(subscribable.ApiName), authMap.get(subscribable.Id));
                    entry.mIsAuthorized = authMap.get(subscribable.Id);
                    user.setSubscriptionCacheEntry(subscribable.ApiName, entry);
                } else {
                    currentEntry.mSubscribable = subscribable;
                    currentEntry.mSubscription = activeSubscriptionsMap.get(subscribable.ApiName);
                    currentEntry.mIsAuthorized = authMap.get(subscribable.Id);

                    user.setSubscriptionCacheEntry(subscribable.ApiName, currentEntry);
                }
            }

            // Check and set access to fishingfacility data so we know this when loading the map later.
            user.setIsFishingFacilityAuthenticated(authMap.get(availableSubscriptionsMap.containsKey(getString(R.string.fishing_facility_api_name)) ? availableSubscriptionsMap.get(getString(R.string.fishing_facility_api_name)).Id : -1));
            user.writeToSharedPref(getActivity());


            for (final PropertyDescription propertyDescription : availableSubscriptions) {
                boolean isAuthed = (authMap.get(propertyDescription.Id) != null ? authMap.get(propertyDescription.Id) : false);

                SubscriptionExpandableListChildObject currentPropertyDescriptionChildObject = setupAvailableSubscriptionChildView(propertyDescription, activeSubscriptionsMap.get(propertyDescription.ApiName), isAuthed);

                availableSubscriptionObjectsList.add(currentPropertyDescriptionChildObject);
            }

            ExpandableListParentObject propertyDescriptionParent = new ExpandableListParentObject();

            propertyDescriptionParent.setChildObjectList(availableSubscriptionObjectsList);
            propertyDescriptionParent.setParentNumber(1);
            propertyDescriptionParent.setParentText(getString(R.string.my_page_all_available_subscriptions));
            propertyDescriptionParent.setResourcePathToImageResource(R.drawable.ikon_kart_til_din_kartplotter);

            parentObjectList.add(propertyDescriptionParent);

            childOnClickListener.setPropertyDescriptions(availableSubscriptions);
        } catch (Exception e) {
            Log.d(TAG, "Exception occured: " + e.toString());
        }

        return parentObjectList;
    }

    public ArrayList<ParentObject> fetchMyPageCached() {
        ArrayList<ParentObject> parentObjectList = new ArrayList<>();
        try {
            Collection<SubscriptionEntry> subscriptionEntries = user.getSubscriptionCacheEntries();

            Map<String, Subscription> activeSubscriptionsMap = new HashMap<>();
            ArrayList<Object> availableSubscriptionObjectsList = new ArrayList<>();
            List<PropertyDescription> availableSubscriptions = new ArrayList<>();
            Map<Integer, Boolean> authMap = new HashMap<>();

            for(SubscriptionEntry subscriptionEntry : subscriptionEntries) {
                availableSubscriptions.add(subscriptionEntry.mSubscribable);
                if(subscriptionEntry.mSubscription != null) {
                    activeSubscriptionsMap.put(subscriptionEntry.mName, subscriptionEntry.mSubscription);
                }
                authMap.put(subscriptionEntry.mSubscribable.Id, subscriptionEntry.mIsAuthorized);
            }

            for (final PropertyDescription propertyDescription : availableSubscriptions) {
                boolean isAuthed = (authMap.get(propertyDescription.Id) != null ? authMap.get(propertyDescription.Id) : false);
                SubscriptionExpandableListChildObject currentPropertyDescriptionChildObject = setupAvailableSubscriptionChildView(propertyDescription, activeSubscriptionsMap.get(propertyDescription.ApiName), isAuthed);

                availableSubscriptionObjectsList.add(currentPropertyDescriptionChildObject);
            }

            ExpandableListParentObject propertyDescriptionParent = new ExpandableListParentObject();

            propertyDescriptionParent.setChildObjectList(availableSubscriptionObjectsList);
            propertyDescriptionParent.setParentNumber(1);
            propertyDescriptionParent.setParentText(getString(R.string.my_page_all_available_subscriptions));
            propertyDescriptionParent.setResourcePathToImageResource(R.drawable.ikon_kart_til_din_kartplotter);

            parentObjectList.add(propertyDescriptionParent);

            childOnClickListener.setPropertyDescriptions(availableSubscriptions);
        } catch (Exception e) {
            Log.d(TAG, "Exception occured: " + e.toString());
        }

        return parentObjectList;
    }

    private SubscriptionExpandableListChildObject setupAvailableSubscriptionChildView(final PropertyDescription subscription, final Subscription activeSubscription, boolean canSubscribe) {
        final SubscriptionExpandableListChildObject currentPropertyDescriptionChildObject = new SubscriptionExpandableListChildObject();

        View.OnClickListener subscriptionSwitchClickListener = (canSubscribe ? onClickListenerInterface.getSubscriptionCheckBoxOnClickListener(subscription, activeSubscription, user) :
                null);

        View.OnClickListener downloadButtonOnClickListener = (canSubscribe ? onClickListenerInterface.getSubscriptionDownloadButtonOnClickListener(subscription, user, TAG) :
                onClickListenerInterface.getInformationDialogOnClickListener(subscription.Name, getString(R.string.unauthorized_user), -1));

        if(!subscription.ErrorType.equals(ApiErrorType.NONE.toString())) {
            View.OnClickListener errorNotificationOnClickListener = onClickListenerInterface.getSubscriptionErrorNotificationOnClickListener(subscription);
            currentPropertyDescriptionChildObject.setErrorNotificationOnClickListener(errorNotificationOnClickListener);
        }

        currentPropertyDescriptionChildObject.setTitleText(subscription.Name);
        currentPropertyDescriptionChildObject.setLastUpdatedText(subscription.LastUpdated.replace("T", "\n"));
        currentPropertyDescriptionChildObject.setIsSubscribed(activeSubscription != null);
        currentPropertyDescriptionChildObject.setAuthorized(canSubscribe);
        currentPropertyDescriptionChildObject.setDownloadButtonOnClickListener(downloadButtonOnClickListener);
        currentPropertyDescriptionChildObject.setSubscribedCheckBoxOnClickListener(subscriptionSwitchClickListener);

        currentPropertyDescriptionChildObject.setErrorType(ApiErrorType.getType(subscription.ErrorType));

        return currentPropertyDescriptionChildObject;
    }


//    private SubscriptionExpandableListChildObject setupWarningChildView(final String subscription) {
//        SubscriptionExpandableListChildObject currentWarningObject = new SubscriptionExpandableListChildObject();
//        currentWarningObject.setTitleText(subscription);
//        currentWarningObject.setLastUpdatedText("");
//        currentWarningObject.setIsSubscribed(true);
//
//        return currentWarningObject;
//    }

    // INFO: we just treat active subscriptions in the same way as we treat available subscriptions, don't see a reason not to.
//    private SubscriptionExpandableListChildObject setupActiveSubscriptionChildView(final Subscription activeSubscription, PropertyDescription subscribable, boolean canSubscribe) {
//        if(subscribable == null) {
//            Log.e(TAG, "subscribable is null");
//            return null;
//        }
//
//        return setupAvailableSubscriptionChildView(subscribable, activeSubscription, canSubscribe);
//    }

    @Override
    public void onRecyclerViewItemExpanded(int position) {
    }

    @Override
    public void onRecyclerViewItemCollapsed(int position) {
    }


    private class ExpandableListAdapterChildOnClickListener implements View.OnClickListener {
        List<PropertyDescription> propertyDescriptions;
        List<String> warnings;
        List<Subscription> subscriptions;

        public ExpandableListAdapterChildOnClickListener() {

        }

        public void setPropertyDescriptions(List<PropertyDescription> propertyDescriptions) {
            this.propertyDescriptions = propertyDescriptions;
        }

//        public void setWarnings(List<String> warnings) {
//            this.warnings = warnings;
//        }
//
//        public void setSubscriptions(List<Subscription> subscriptions) {
//            this.subscriptions = subscriptions;
//        }

        @Override
        public void onClick(View v) {
            String identifier = ((ViewGroup) v.getParent()).getTag().toString();
            boolean found = false;
            JsonObject object = null;
            Gson gson = new Gson();
            String type = "";
            for (PropertyDescription pd : propertyDescriptions) {
                if (pd.Name.equals(identifier)) {
                    found = true;
                    object = (new JsonParser()).parse(gson.toJson(pd)).getAsJsonObject();
                    type += "pd";
                    break;
                }
            }

            if (!found) {
                for (String warning : warnings) {
                    if (warning.equals(identifier)) {
                        found = true;
                        //TODO: Generate object proper
                        object = new JsonObject();
                        type += "warning";
                        break;
                    }
                }
            }
            if (!found) {
                for (Subscription subscription : subscriptions) {
                    if (subscription.GeoDataServiceName.equals(identifier)) {
                        object = (new JsonParser()).parse(gson.toJson(subscription)).getAsJsonObject();
                        type += "sub";
                        break;
                    }
                }
            }

            if (object == null) {
                Log.d(TAG, "We failed at retrieving the object: ");
            }

            getFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, createFragment(object, type), CardViewFragment.TAG).addToBackStack(null).
                    commit();
        }
    }

    private Fragment createFragment(JsonObject object, String type) {
        Bundle userBundle = new Bundle();
        userBundle.putParcelable("user", user);
        userBundle.putString("type", type);
        userBundle.putString("args", object.toString());
        CardViewFragment cardViewFragment = CardViewFragment.newInstance();
        cardViewFragment.setArguments(userBundle);
        return cardViewFragment;
    }
}
