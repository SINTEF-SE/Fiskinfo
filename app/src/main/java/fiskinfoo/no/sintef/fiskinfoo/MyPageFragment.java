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
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ExpandableListParentObject;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionExpandableListChildObject;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.BarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.IBarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.Authorization;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.PropertyDescription;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.Subscription;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.ApiErrorType;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskInfoUtility;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.MyPageExpandableListAdapter;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.User;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityDialogs;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityOnClickListeners;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityRows;
import fiskinfoo.no.sintef.fiskinfoo.Interface.DialogInterface;
import fiskinfoo.no.sintef.fiskinfoo.Interface.UtilityRowsInterface;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ExpandCollapseListener;
import fiskinfoo.no.sintef.fiskinfoo.View.MaterialExpandableList.ParentObject;

/**
 * TODO: Retain instance on orientation (Alot of work)
 */
public class MyPageFragment extends Fragment implements ExpandCollapseListener {
    FragmentActivity listener;
    public static final String TAG = "MyPageFragment";
    private User user;
    private MyPageExpandableListAdapter myPageExpandableListAdapter;
    private ExpandableListAdapterChildOnClickListener childOnClickListener;
    private RecyclerView mCRecyclerView;
    private DialogInterface dialogInterface;
    private UtilityRowsInterface utilityRowsInterface;
    private UtilityOnClickListeners onClickListenerInterface;
    private FiskInfoUtility fiskInfoUtility;

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
        dialogInterface = new UtilityDialogs();
        utilityRowsInterface = new UtilityRows();
        onClickListenerInterface = new UtilityOnClickListeners();
        fiskInfoUtility = new FiskInfoUtility();
    }


    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
        View v = inf.inflate(R.layout.fragment_my_page, parent, false);
        ArrayList<ParentObject> data = fetchMyPage();
        myPageExpandableListAdapter = new MyPageExpandableListAdapter(this.getActivity(), data, childOnClickListener);
        myPageExpandableListAdapter.addExpandCollapseListener(this);
        myPageExpandableListAdapter.onRestoreInstanceState(savedInstanceState);
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
        }

        //myPageExpandableListAdapter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            Log.d(TAG, outState.toString());
        }
        if(mCRecyclerView != null) {
            ((MyPageExpandableListAdapter) mCRecyclerView.getAdapter()).onSaveInstanceState(outState);
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
            List<String> myWarnings = new ArrayList<>(); //TODO: Tie in polar low warning
            List<Subscription> activeSubscriptions = api.getSubscriptions();
            List<Authorization> authorizations = api.getAuthorization();
            Map<Integer, Boolean> authMap = new HashMap<>();
            Map<String, PropertyDescription> availableSubscriptionsMap = new HashMap<>();
            Map<String, Subscription> activeSubscriptionsMap = new HashMap<>();


            for(PropertyDescription subscribable : api.getSubscribable()) {
                availableSubscriptionsMap.put(subscribable.ApiName, subscribable);
            }

            for(Subscription subscription : api.getSubscriptions()) {
                activeSubscriptionsMap.put(subscription.GeoDataServiceName, subscription);
            }

            for(Authorization auth : authorizations) {
                authMap.put(auth.Id, auth.hasAccess);
                System.out.println("Id: " + auth.Id + ", access: " + auth.hasAccess);
            }

            ArrayList<Object> availableSubscriptionObjectsList = new ArrayList<>();
            for (final PropertyDescription propertyDescription : availableSubscriptions) {
                //TODO: uncomment after fixing getAuthorization to read correct values.
//                if(!authMap.get(propertyDescription.Id)) {
//                    continue;
//                }

                SubscriptionExpandableListChildObject currentPropertyDescriptionChildObject = setupAvailableSubscriptionChildView(propertyDescription, activeSubscriptionsMap.get(propertyDescription.ApiName));

                availableSubscriptionObjectsList.add(currentPropertyDescriptionChildObject);
            }

            ArrayList<Object> warningServiceChildObjectList = new ArrayList<>();
            for (String warning : myWarnings) {
                SubscriptionExpandableListChildObject currentWarning = setupWarningChildView(warning);

                warningServiceChildObjectList.add(currentWarning);
            }

            ArrayList<Object> mySubscriptions = new ArrayList<>();
            for (Subscription subscription : activeSubscriptions) {
                SubscriptionExpandableListChildObject currentSubscription = setupActiveSubscriptionChildView(subscription, availableSubscriptionsMap.get(subscription.GeoDataServiceName));

                mySubscriptions.add(currentSubscription);
            }

            ExpandableListParentObject propertyDescriptionParent = new ExpandableListParentObject();
            ExpandableListParentObject warningParent = new ExpandableListParentObject();
            ExpandableListParentObject subscriptionParent = new ExpandableListParentObject();


            propertyDescriptionParent.setChildObjectList(availableSubscriptionObjectsList);
            propertyDescriptionParent.setParentNumber(1);
            propertyDescriptionParent.setParentText(getString(R.string.my_page_all_available_subscriptions));
            propertyDescriptionParent.setResourcePathToImageResource(R.drawable.ikon_kart_til_din_kartplotter);

            warningParent.setChildObjectList(warningServiceChildObjectList);
            warningParent.setParentNumber(2);
            warningParent.setParentText(getString(R.string.my_page_all_warnings));
            warningParent.setResourcePathToImageResource(R.drawable.ikon_varsling_av_polare_lavtrykk);

            subscriptionParent.setChildObjectList(mySubscriptions);
            subscriptionParent.setParentNumber(3);
            subscriptionParent.setParentText(getString(R.string.my_page_my_subscriptions));
            subscriptionParent.setResourcePathToImageResource(R.drawable.ikon_kart_til_din_kartplotter);

            parentObjectList.add(propertyDescriptionParent);
            parentObjectList.add(warningParent);
            parentObjectList.add(subscriptionParent);

            childOnClickListener.setSubscriptions(activeSubscriptions);
            childOnClickListener.setWarnings(myWarnings);
            childOnClickListener.setPropertyDescriptions(availableSubscriptions);

        } catch (Exception e) {
            Log.d(TAG, "Exception occured: " + e.toString());
        }

        return parentObjectList;
    }

    private SubscriptionExpandableListChildObject setupAvailableSubscriptionChildView(final PropertyDescription subscription, final Subscription activeSubscription) {
        final SubscriptionExpandableListChildObject currentPropertyDescriptionChildObject = new SubscriptionExpandableListChildObject();

        View.OnClickListener errorNotificationOnClickListener = onClickListenerInterface.getErrorNotificationOnClickListener(subscription);
        View.OnClickListener subscriptionSwitchClickListener = onClickListenerInterface.getSubscriptionSwitchOnClickListener(subscription, activeSubscription, user);
        View.OnClickListener downloadButtonOnClickListener = onClickListenerInterface.getSubscriptionDownloadButtonOnClickListener(subscription, user, TAG);

        currentPropertyDescriptionChildObject.setTitleText(subscription.Name);
        currentPropertyDescriptionChildObject.setLastUpdatedText(subscription.LastUpdated.replace("T", "\n"));
        currentPropertyDescriptionChildObject.setIsSubscribed(activeSubscription != null);
        currentPropertyDescriptionChildObject.setDownloadButtonOnClickListener(downloadButtonOnClickListener);
        currentPropertyDescriptionChildObject.setSubscribeSwitchOnClickListener(subscriptionSwitchClickListener);

        currentPropertyDescriptionChildObject.setErrorType(ApiErrorType.getType(subscription.ErrorType));
        currentPropertyDescriptionChildObject.setErrorNotificationOnClickListener(errorNotificationOnClickListener);

        return currentPropertyDescriptionChildObject;
    }


    private SubscriptionExpandableListChildObject setupWarningChildView(final String subscription) {
        SubscriptionExpandableListChildObject currentWarningObject = new SubscriptionExpandableListChildObject();
        currentWarningObject.setTitleText(subscription);
        currentWarningObject.setLastUpdatedText("");
        currentWarningObject.setIsSubscribed(true);

        return currentWarningObject;
    }

    // INFO: we just treat active subscriptions in the same way as we treat available subscriptions, don't see a reason not to.
    private SubscriptionExpandableListChildObject setupActiveSubscriptionChildView(final Subscription activeSubscription, PropertyDescription subscribable) {
        if(subscribable == null) {
            Log.e(TAG, "subscribable is null");
            return null;
        }

        return setupAvailableSubscriptionChildView(subscribable, activeSubscription);
    }

    @Override
    public void onRecyclerViewItemExpanded(int position) {
        Toast.makeText(this.getActivity(), "Item Expanded " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRecyclerViewItemCollapsed(int position) {
        Toast.makeText(this.getActivity(), "Item Collapsed " + position, Toast.LENGTH_SHORT).show();
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

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public void setSubscriptions(List<Subscription> subscriptions) {
            this.subscriptions = subscriptions;
        }

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
                        found = true;
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
                    replace(R.id.fragment_placeholder, createFragment(object, type), CardViewFragment.TAG).addToBackStack(null).
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
