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

package fiskinfoo.no.sintef.fiskinfoo.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.FiskInfoPolygon2D;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.IceConcentrationType;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.LayerAndVisibility;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.Line;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.Point;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.Polygon;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionEntry;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ToolType;
import fiskinfoo.no.sintef.fiskinfoo.FiskInfo;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.BarentswatchApi;
import fiskinfoo.no.sintef.fiskinfoo.Http.BarentswatchApiRetrofit.models.PropertyDescription;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskInfoUtility;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskinfoScheduledTaskExecutor;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.GeometryType;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.GpsLocationTracker;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.User;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityDialogs;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityOnClickListeners;
import fiskinfoo.no.sintef.fiskinfoo.Interface.UserInterface;
import fiskinfoo.no.sintef.fiskinfoo.MainActivity;
import fiskinfoo.no.sintef.fiskinfoo.R;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.CheckBoxRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.ToolLegendRow;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static fiskinfoo.no.sintef.fiskinfoo.MainActivity.MY_PERMISSIONS_REQUEST_FINE_LOCATION;
import static fiskinfoo.no.sintef.fiskinfoo.MainActivity.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class MapFragment extends Fragment {
    public static final String FRAGMENT_TAG = "MapFragment";

    private AutoCompleteTextView searchEditText;
    private LinearLayout bottomSheetLayout;
    private LinearLayout bottomSheetToolLayout;
    private LinearLayout bottomSheetSeismicLayout;
    private LinearLayout bottomSheetSeaFloorInstallationLayout;
    private LinearLayout bottomSheetIceConcentrationLayout;
    private LinearLayout bottomSheetJMessageLayout;
    private LinearLayout bottomSheetInformationContainer;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView bottomSheetToolTypeTextView;
    private TextView bottomSheetToolSetupDateTextView;
    private TextView bottomSheetToolVesselTextView;
    private TextView bottomSheetToolPhoneNumberTextView;
    private TextView bottomSheetToolPositionTextView;
    private TextView bottomSheetToolMarinogramTextView;
    private TextView bottomSheetSeismicAreaTypeTextView;
    private TextView bottomSheetSeismicPeriodTextView;
    private TextView bottomSheetSeismicStartDateTextView;
    private TextView bottomSheetSeismicVesselTextView;
    private TextView bottomSheetSeismicCompanyTextView;
    private TextView bottomSheetSeismicTypeTextView;
    private TextView bottomSheetSeismicFactLinkTextView;
    private TextView bottomSheetSeismicMapLinkTextView;
    private TextView bottomSheetSeismicAisTrackingTextView;
    private TextView bottomSheetSeaFloorInstallationNameTextView;
    private TextView bottomSheetSeaFloorInstallationTypeTextView;
    private TextView bottomSheetSeaFloorInstallationFunctionTextView;
    private TextView bottomSheetSeaFloorInstallationDepthTextView;
    private TextView bottomSheetSeaFloorInstallationFieldTextView;
    private TextView bottomSheetSeaFloorInstallationStartupDateTextView;
    private TextView bottomSheetSeaFloorInstallationOperatorTextView;
    private TextView bottomSheetSeaFloorInstallationPositionTextView;
    private TextView bottomSheetSeaFloorInstallationMarinogramTextView;
    private TextView bottomSheetIceConcentrationTypeTextView;
    private TextView bottomSheetIceConcentrationSatelliteImagesLinkTextView;
    private TextView bottomSheetIceConcentrationMetIceInformationTextView;
    private TextView bottomSheetJMessageTitleTextView;
    private TextView bottomSheetJMessageDescriptionTextView;
    private TextView bottomSheetJMessageStartDateTextView;
    private TextView bottomSheetJMessageClosedForTextView;
    private TextView bottomSheetJMessageFishGroupTextView;
    private TextView bottomSheetJMessageAreaTextView;
    private TextView bottomSheetJMessageFisheryMessagesLinkTextView;
    private TextView bottomSheetJMessageJMessagesLinkTextView;
    private TextView bottomSheetJMessageVersionTextView;
    private TextView bottomSheetJMessageDataSourceTextView;
    private TextView bottomSheetJMessageFjordLinesDetailsTextView;
    private TextView bottomSheetJMessageLastUpdatedTextView;
    private TextView bottomSheetJMessageLinksTitleTextView;
    private TextView bottomSheetInformationContainerHeaderTextView;
    private LinearLayout bottomSheetJMessageLinksLinearLayout;
    private LinearLayout bottomSheetJMessageDataSourcesLinearLayout;
    private NestedScrollView bottomSheetJMessageFjordLinesDetailsScrollView;

    private AsynchApiCallTask asynchApiCallTask;
    private WebView browser;
    private BarentswatchApi barentswatchApi;
    private User user;
    private FiskInfoUtility fiskInfoUtility;
    private UtilityDialogs dialogInterface;
    private UtilityOnClickListeners onClickListenerInterface;
    private ScheduledFuture proximityAlertWatcher;
    private GpsLocationTracker mGpsLocationTracker;
    private UserInterface userInterface;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private FiskInfoPolygon2D tools = null;
    private JSONArray layersAndVisibility = null;
    private boolean cacheDeserialized = false;
    private boolean alarmFiring = false;
    protected double cachedLat;
    protected double cachedLon;
    protected double cachedDistance;
    private Map<String, JSONObject> toolMap;
    private JSONObject toolsFeatureCollection;
    private Map<String, List<Integer>> vesselToolIdsMap =  new HashMap<>();
    private boolean pageLoaded = false;
    private Tracker tracker;


    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FiskInfo application = (FiskInfo) getActivity().getApplication();
        tracker = application.getDefaultTracker();

        user = userInterface.getUser();
        if (user == null) {
            Log.d(FRAGMENT_TAG, "Could not get user");
        }

        toolMap = new HashMap<>();
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UserInterface) {
            userInterface = (UserInterface) getActivity();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        userInterface = null;
        if(asynchApiCallTask != null && asynchApiCallTask.getStatus() != AsyncTask.Status.RUNNING) {
            asynchApiCallTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getView() != null) {
            getView().refreshDrawableState();
        }

        MainActivity activity = (MainActivity) getActivity();
        String title = getResources().getString(R.string.map_fragment_title);
        activity.refreshTitle(title);

        if(tracker != null){

            tracker.setScreenName(getClass().getSimpleName());
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        for (int i = 0; i < menu.size(); i++) {
            menu.removeItem(i);
        }
        inflater.inflate(R.menu.menu_map, menu);
    }

    // This event fires 3rd, and is the first time views are available in the fragment
    // The onCreateView method is called when Fragment should create its View object hierarchy.
    // Use onCreateView to get a handle to views as soon as they are freshly inflated
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {

        final View rootView = inf.inflate(R.layout.fragment_map, parent, false);;

        searchEditText = (AutoCompleteTextView) rootView.findViewById(R.id.map_fragment_tool_search_edit_text);
        bottomSheetLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_bottom_sheet);
        bottomSheetToolLayout = (LinearLayout) bottomSheetLayout.findViewById(R.id.linear_layout_bottom_sheet_tool_information_container);
        bottomSheetSeismicLayout = (LinearLayout) bottomSheetLayout.findViewById(R.id.linear_layout_bottom_sheet_seismic_information_container);
        bottomSheetSeaFloorInstallationLayout = (LinearLayout) bottomSheetLayout.findViewById(R.id.linear_layout_bottom_sheet_sea_floor_installation_information_container);
        bottomSheetIceConcentrationLayout = (LinearLayout) bottomSheetLayout.findViewById(R.id.linear_layout_bottom_sheet_ice_concentration_information_container);
        bottomSheetJMessageLayout = (LinearLayout) bottomSheetLayout.findViewById(R.id.linear_layout_bottom_sheet_j_melding_information_container);
        bottomSheetInformationContainer = (LinearLayout) bottomSheetLayout.findViewById(R.id.linear_layout_bottom_sheet_information_container);

        bottomSheetToolTypeTextView = (TextView) bottomSheetToolLayout.findViewById(R.id.map_fragment_bottom_sheet_tool_type_text_view);
        bottomSheetToolSetupDateTextView = (TextView) bottomSheetToolLayout.findViewById(R.id.map_fragment_bottom_sheet_tool_setup_date_text_view);
        bottomSheetToolVesselTextView = (TextView) bottomSheetToolLayout.findViewById(R.id.map_fragment_bottom_sheet_tool_vessel_text_view);
        bottomSheetToolPhoneNumberTextView = (TextView) bottomSheetToolLayout.findViewById(R.id.map_fragment_bottom_sheet_tool_phone_text_view);
        bottomSheetToolPositionTextView = (TextView) bottomSheetToolLayout.findViewById(R.id.map_fragment_bottom_sheet_tool_position_text_view);
        bottomSheetToolMarinogramTextView = (TextView) bottomSheetToolLayout.findViewById(R.id.map_fragment_bottom_sheet_tool_marinogram_text_view);

        bottomSheetSeismicAreaTypeTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_type_type_text_view);
        bottomSheetSeismicPeriodTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_period_text_view);
        bottomSheetSeismicStartDateTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_start_date_text_view);
        bottomSheetSeismicVesselTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_vessel_text_view);
        bottomSheetSeismicCompanyTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_company_text_view);
        bottomSheetSeismicTypeTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_type_text_view);
        bottomSheetSeismicFactLinkTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_fact_link_text_view);
        bottomSheetSeismicMapLinkTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_map_link_text_view);
        bottomSheetSeismicAisTrackingTextView = (TextView) bottomSheetSeismicLayout.findViewById(R.id.map_fragment_bottom_sheet_seismic_ais_text_view);

        bottomSheetSeaFloorInstallationNameTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_name_text_view);
        bottomSheetSeaFloorInstallationTypeTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_type_type_text_view);
        bottomSheetSeaFloorInstallationFunctionTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_function_text_view);
        bottomSheetSeaFloorInstallationDepthTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_depth_text_view);
        bottomSheetSeaFloorInstallationFieldTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_field_text_view);
        bottomSheetSeaFloorInstallationStartupDateTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_start_date_text_view);
        bottomSheetSeaFloorInstallationOperatorTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_operator_text_view);
        bottomSheetSeaFloorInstallationPositionTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_position_text_view);
        bottomSheetSeaFloorInstallationMarinogramTextView = (TextView) bottomSheetSeaFloorInstallationLayout.findViewById(R.id.map_fragment_bottom_sheet_sea_floor_installation_marinogram_text_view);
        bottomSheetIceConcentrationTypeTextView = (TextView) bottomSheetIceConcentrationLayout.findViewById(R.id.linear_layout_bottom_sheet_ice_concentration_ice_type_text_view);
        bottomSheetIceConcentrationSatelliteImagesLinkTextView = (TextView) bottomSheetIceConcentrationLayout.findViewById(R.id.linear_layout_bottom_sheet_ice_concentration_satellite_images_link_text_view);
        bottomSheetIceConcentrationMetIceInformationTextView = (TextView) bottomSheetIceConcentrationLayout.findViewById(R.id.linear_layout_bottom_sheet_ice_concentration_met_ice_information_link_text_view);

        bottomSheetJMessageTitleTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_message_title_text_view);
        bottomSheetJMessageDescriptionTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_description_text_view);
        bottomSheetJMessageStartDateTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_start_date_text_view);
        bottomSheetJMessageClosedForTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_closed_for_text_view);
        bottomSheetJMessageFishGroupTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_fish_group_text_view);
        bottomSheetJMessageAreaTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_area_text_view);
        bottomSheetJMessageVersionTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_version_text_view);
        bottomSheetJMessageFisheryMessagesLinkTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_fishing_messages_info_text_view);
        bottomSheetJMessageJMessagesLinkTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_j_messages_info_text_view);
        bottomSheetJMessageDataSourceTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_data_source_text_view);
        bottomSheetJMessageLastUpdatedTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_last_updated_text_view);
        bottomSheetJMessageLinksTitleTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_links_title_text_view);
        bottomSheetJMessageLinksLinearLayout = (LinearLayout) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_links_linear_layout);
        bottomSheetJMessageDataSourcesLinearLayout = (LinearLayout) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_data_source_linear_layout);
        bottomSheetJMessageFjordLinesDetailsScrollView = (NestedScrollView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_fjord_lines_details_scroll_view);
        bottomSheetJMessageFjordLinesDetailsTextView = (TextView) bottomSheetJMessageLayout.findViewById(R.id.map_fragment_bottom_sheet_j_melding_fjord_lines_details_text_view);

        bottomSheetInformationContainerHeaderTextView = (TextView) bottomSheetLayout.findViewById(R.id.map_fragment_bottom_sheet_header_text_view);


        // TODO: Disable search if user is not authenticated

        searchEditText.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.text_white_transparent));

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch(i) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        List<Integer> vesselTools = vesselToolIdsMap.get(textView.getText().toString());
                        highlightVesselInMap(textView.getText().toString());
                        searchEditText.setTag(getString(R.string.map_search_view_tag_clear));
                        searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_black_24dp, 0);
                        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });

        searchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (searchEditText.getRight() - searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if(getString(R.string.map_search_view_tag_search).equals(searchEditText.getTag())) {
                            searchEditText.setError(null);

                            if(vesselToolIdsMap.get(searchEditText.getText().toString().toUpperCase()) != null) {
                                searchEditText.setTag(getString(R.string.map_search_view_tag_clear));
                                searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_black_24dp, 0);

                                highlightVesselInMap(searchEditText.getText().toString().toUpperCase());
                                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                            } else{
                                searchEditText.setError(getString(R.string.error_no_match_for_vessel_search));
                            }
                        } else{
                            searchEditText.setTag(getString(R.string.map_search_view_tag_search));
                            searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search_black_24dp, 0);

                            String nullString = null;
                            highlightVesselInMap(null);
                            searchEditText.setText("");
                            InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                        }


                        return true;
                    }
                }
                return false;
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        return rootView;
    }

    // This fires 4th, and this is the first time the Activity is fully created.
    // Accessing the view hierarchy of the parent activity must be done in the onActivityCreated
    // At this point, it is safe to search for activity View objects by their ID, for example.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fiskInfoUtility = new FiskInfoUtility();
        barentswatchApi = new BarentswatchApi();
        dialogInterface = new UtilityDialogs();
        onClickListenerInterface = new UtilityOnClickListeners();
        configureWebParametersAndLoadDefaultMapApplication();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        browser.reload();
        super.onConfigurationChanged(newConfig);
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void configureWebParametersAndLoadDefaultMapApplication() {
        if(getView() == null) {
            throw new NullPointerException();
        }
        browser = (WebView) getView().findViewById(R.id.map_fragment_web_view);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setDomStorageEnabled(true);
        browser.getSettings().setGeolocationEnabled(true);
        browser.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        browser.addJavascriptInterface(new JavaScriptInterface(getActivity()), "Android");
        browser.setWebViewClient(new barentswatchFiskInfoWebClient());
        browser.setWebChromeClient(new WebChromeClient() {

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                Log.d("geolocation permission", "permission >>>" + origin);
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(FRAGMENT_TAG, message);
                return super.onJsAlert(view, url, message, result);
            }

        });

        updateMap();
    }

    public class JavaScriptInterface {
        Context mContext;

        JavaScriptInterface(Context context) {
            mContext = context;
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public String getToken() {
            return user.getToken();
        }

        @android.webkit.JavascriptInterface
        public void setMessage(String message) {
            Log.d(FRAGMENT_TAG, message);
            try {
                layersAndVisibility = new JSONArray(message);
            } catch (Exception e) {
                e.printStackTrace();
                //TODO
            }
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public void openMarinogramUrl(String url) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public void hideBottomSheet() {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public void updateToolBottomSheet(String toolId) {
            final JSONObject tool = toolMap.get(toolId);

            if(tool == null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomSheetSeismicLayout.setVisibility(View.GONE);
                    bottomSheetIceConcentrationLayout.setVisibility(View.GONE);
                    bottomSheetJMessageLayout.setVisibility(View.GONE);
                    bottomSheetSeaFloorInstallationLayout.setVisibility(View.GONE);
                    bottomSheetInformationContainer.setVisibility(View.GONE);
                    bottomSheetToolLayout.setVisibility(View.VISIBLE);

                    try {
                        final String vesselName = tool.getJSONObject("properties").getString("vesselname");
                        String setupDateString = tool.getJSONObject("properties").getString("setupdatetime");
                        Date setupDate;
                        String marinogramUrl;
                        String vesselPhone = tool.getJSONObject("properties").getString("vesselphone");
                        vesselPhone = vesselPhone != null && !"null".equals(vesselPhone) ? vesselPhone : null;

                        setupDateString = (setupDateString != null && setupDateString.length() > 19) ? setupDateString.substring(0, 19) : setupDateString;

                        SimpleDateFormat sdf = new SimpleDateFormat(getContext().getString(R.string.datetime_format_yyyy_mm_dd_t_hh_mm_ss), Locale.getDefault());
                        SimpleDateFormat sdfSetDate = new SimpleDateFormat(getContext().getString(R.string.datetime_format_yyyy_mm_dd_hh_mm), Locale.getDefault());

                        sdf.setTimeZone(TimeZone.getDefault());

                        try {
                            setupDate = sdf.parse(setupDateString);
                            setupDateString = sdfSetDate.format(setupDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        bottomSheetToolTypeTextView.setText(ToolType.createFromValue(tool.getJSONObject("properties").getString("tooltypecode")).toString());
                        bottomSheetToolSetupDateTextView.setText(setupDateString);
                        bottomSheetToolVesselTextView.setText(vesselName);
                        bottomSheetToolPhoneNumberTextView.setText(vesselPhone);
                        bottomSheetToolPhoneNumberTextView.setVisibility(vesselPhone != null ? View.VISIBLE : View.GONE);

                        bottomSheetToolVesselTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.hyperlink_blue));
                        bottomSheetToolVesselTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        bottomSheetToolVesselTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                searchEditText.setError(null);
                                searchEditText.setText(((TextView)view).getText().toString());
                                searchEditText.setTag(getString(R.string.map_search_view_tag_clear));
                                searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_black_24dp, 0);
                                highlightVesselInMap(vesselName);
                            }
                        });

                        JSONArray toolJsonCoordinates = tool.getJSONObject("geometry").getJSONArray("coordinates");
                        if(tool.getJSONObject("geometry").getString("type").equals(GeometryType.LINESTRING.toString())) {
                            String dmsPosition = FiskInfoUtility.decimalToDMS(toolJsonCoordinates.getJSONArray(0).getDouble(1));
                            dmsPosition += " " + toolJsonCoordinates.getJSONArray(0).getDouble(0);
                            marinogramUrl = "http://www.yr.no/sted/hav/" + String.format(Locale.ENGLISH , "%.8f", toolJsonCoordinates.getJSONArray(0).getDouble(1)) + "_" +
                                    String.format(Locale.ENGLISH , "%.8f", toolJsonCoordinates.getJSONArray(0).getDouble(0));

                            bottomSheetToolPositionTextView.setText(dmsPosition);
                        } else {
                            marinogramUrl = "http://www.yr.no/sted/hav/" + String.format(Locale.ENGLISH , "%.8f", toolJsonCoordinates.getDouble(1)) + "_" +
                                    String.format(Locale.ENGLISH , "%.8f", toolJsonCoordinates.getDouble(0));
                            String dmsPosition = FiskInfoUtility.decimalToDMS(toolJsonCoordinates.getDouble(1));
                            dmsPosition += " " + FiskInfoUtility.decimalToDMS(toolJsonCoordinates.getDouble(0));

                            bottomSheetToolPositionTextView.setText(dmsPosition);
                        }

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bottomSheetToolMarinogramTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(marinogramUrl, getString(R.string.show_marinogram_from_yr)), Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            bottomSheetToolMarinogramTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(marinogramUrl, getString(R.string.show_marinogram_from_yr))));
                        }

                        bottomSheetToolMarinogramTextView.setMovementMethod(LinkMovementMethod.getInstance());


                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    bottomSheetBehavior.setPeekHeight(200);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }
            });
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void updateAISBottomSheet(final String jsonString) {
            if(jsonString == null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                Toast.makeText(getContext(), R.string.toast_no_ais_data_found, Toast.LENGTH_LONG).show();
                return;
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final JSONObject finalJsonObject = jsonObject;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomSheetToolLayout.setVisibility(View.GONE);
                    bottomSheetSeaFloorInstallationLayout.setVisibility(View.GONE);
                    bottomSheetIceConcentrationLayout.setVisibility(View.GONE);
                    bottomSheetSeismicLayout.setVisibility(View.GONE);
                    bottomSheetJMessageLayout.setVisibility(View.GONE);
                    bottomSheetInformationContainer.setVisibility(View.VISIBLE);

                    bottomSheetInformationContainer.removeViews(1, bottomSheetInformationContainer.getChildCount() - 1);

                    try {
                        String vesselHeader = finalJsonObject.getJSONObject("properties").getString("Name");
                        bottomSheetInformationContainerHeaderTextView.setText(vesselHeader);

                        TextView vesselSpeedTextView = new TextView(getContext());
                        TextView vesselCourseTextView = new TextView(getContext());
                        TextView vesselDestinationTextView = new TextView(getContext());
                        TextView vesselToolsTextView = new TextView(getContext());
                        TextView vesselFlagTextView = new TextView(getContext());
                        TextView vesselIRCSTextView = new TextView(getContext());
                        TextView vesselIMOTextView = new TextView(getContext());
                        TextView vesselMMSITextView = new TextView(getContext());
                        LinearLayout vesselToolsContainer = new LinearLayout(getContext());
                        vesselToolsContainer.setOrientation(LinearLayout.VERTICAL);

                        vesselSpeedTextView.setText(getString(R.string.vessel_speed, finalJsonObject.getJSONObject("properties").getString("Sog")));
                        vesselCourseTextView.setText(getString(R.string.vessel_course, finalJsonObject.getJSONObject("properties").getString("Cog")));
                        vesselDestinationTextView.setText(getString(R.string.vessel_destination, finalJsonObject.getJSONObject("properties").getString("Destination")));
                        vesselFlagTextView.setText(getString(R.string.vessel_flag, finalJsonObject.getJSONObject("properties").getString("Country")));
                        vesselIRCSTextView.setText(getString(R.string.vessel_ircs, finalJsonObject.getJSONObject("properties").getString("Callsign")));
                        vesselIMOTextView.setText(getString(R.string.vessel_imo, finalJsonObject.getJSONObject("properties").getString("Imo")));
                        vesselMMSITextView.setText(getString(R.string.vessel_mmsi, finalJsonObject.getJSONObject("properties").getString("Mmsi")));
                        vesselToolsTextView.setText(R.string.tool);

                        vesselSpeedTextView.setTypeface(null, Typeface.BOLD);
                        vesselCourseTextView.setTypeface(null, Typeface.BOLD);
                        vesselToolsTextView.setTypeface(null, Typeface.BOLD);

                        // TODO: Add tools associated with vessel to tool list if applicable

                        if(vesselToolIdsMap.get(finalJsonObject.getJSONObject("properties").getString("Name")) != null) {
                            for(int toolId : vesselToolIdsMap.get(finalJsonObject.getJSONObject("properties").getString("Name"))) {
                                JSONObject tool = ((JSONObject)toolsFeatureCollection.getJSONArray("features").get(toolId));
                                String toolType = ToolType.createFromValue( tool.getJSONObject("properties").getString("tooltypename")).toString();
                                String toolSetupDate = tool.getJSONObject("properties").getString("setupdatetime");
                                toolSetupDate = toolSetupDate.replace("T" , " ");
                                toolSetupDate = "\t" + toolType + " satt " + toolSetupDate.substring(0, 19);

                                TextView toolTextView = new TextView(getContext());
                                toolTextView.setText(toolSetupDate);
                                vesselToolsContainer.addView(toolTextView);
                            }
                        }

                        bottomSheetInformationContainer.addView(vesselSpeedTextView);
                        bottomSheetInformationContainer.addView(vesselCourseTextView);

                        if(!"null".equals(finalJsonObject.getJSONObject("properties").getString("Destination"))) {
                            bottomSheetInformationContainer.addView(vesselDestinationTextView);
                        }
                        if(vesselToolsContainer.getChildCount() > 0) {
                            bottomSheetInformationContainer.addView(vesselToolsTextView);
                            bottomSheetInformationContainer.addView(vesselToolsContainer);

                            LinearLayout buttonContainer = new LinearLayout(getContext());
                            Button highlightToolsButton = new Button(getContext());
                            Button centerVesselButton = new Button(getContext());

                            buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
                            highlightToolsButton.setText(getString(R.string.focus_tools));
                            centerVesselButton.setText(getString(R.string.center_vessel));

                            highlightToolsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        highlightToolsInMap(finalJsonObject.getJSONObject("properties").getString("Name"));
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                        bottomSheetBehavior.setPeekHeight(200);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            centerVesselButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        centerVesselInMap(finalJsonObject.getJSONObject("properties").getString("Name"));
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                        bottomSheetBehavior.setPeekHeight(200);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            buttonContainer.addView(highlightToolsButton);
                            buttonContainer.addView(centerVesselButton);
                            bottomSheetInformationContainer.addView(buttonContainer);
                        }
                        if(!"null".equals(finalJsonObject.getJSONObject("properties").getString("Country"))) {
                            bottomSheetInformationContainer.addView(vesselFlagTextView);
                        }
                        if(!"null".equals(finalJsonObject.getJSONObject("properties").getString("Callsign"))) {
                            bottomSheetInformationContainer.addView(vesselIRCSTextView);
                        }
                        if(!"null".equals(finalJsonObject.getJSONObject("properties").getString("Mmsi"))) {
                            bottomSheetInformationContainer.addView(vesselMMSITextView);
                        }
                        if(!"null".equals(finalJsonObject.getJSONObject("properties").getString("Imo"))) {
                            bottomSheetInformationContainer.addView(vesselIMOTextView);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    bottomSheetBehavior.setPeekHeight(200);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void updateJMeldingBottomSheet(final String jsonString) {

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(jsonObject == null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }

            final JSONObject finalJsonObject = jsonObject;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomSheetToolLayout.setVisibility(View.GONE);
                    bottomSheetSeaFloorInstallationLayout.setVisibility(View.GONE);
                    bottomSheetIceConcentrationLayout.setVisibility(View.GONE);
                    bottomSheetSeismicLayout.setVisibility(View.GONE);
                    bottomSheetInformationContainer.setVisibility(View.GONE);
                    bottomSheetJMessageLayout.setVisibility(View.VISIBLE);

                    for(int i = 0; i < bottomSheetJMessageLayout.getChildCount(); i++) {
                        bottomSheetJMessageLayout.getChildAt(i).setVisibility(View.GONE);
                    }

                    try {
                        if(finalJsonObject.getJSONObject("properties").has("jmelding_name")) {
                            String name = finalJsonObject.getJSONObject("properties").getString("name");
                            String description = finalJsonObject.getJSONObject("properties").getString("description");
                            String closedFrom = finalJsonObject.getJSONObject("properties").getString("closed_date");
                            String closedTo = finalJsonObject.getJSONObject("properties").getString("opened_date");
                            String closedFor = getString(R.string.closed_for, finalJsonObject.getJSONObject("properties").getString("type_name"));
                            String fishGroup = getString(R.string.fish_group, finalJsonObject.getJSONObject("properties").getString("speciestype_name"));
                            String area = getString(R.string.area, finalJsonObject.getJSONObject("properties").getString("area_name"));
                            String version = getString(R.string.j_message_version, finalJsonObject.getJSONObject("properties").getString("jmelding_name"), finalJsonObject.getJSONObject("properties").getString("jmelding_paragraph_count"));
                            String jMessagesUrl = getString(R.string.active_j_message_base_url, finalJsonObject.getJSONObject("properties").getString("jmelding_name"), finalJsonObject.getJSONObject("properties").getString("jmelding_paragraph_count"));
                            Date date;

//                        closedFrom = (closedFrom != null && closedFrom.length() > 10) ? closedFrom.substring(0, 10) : closedFrom;
                            closedTo = (closedTo != null && closedTo.length() > 10) ? closedTo.substring(0, 10) : closedTo;

                            SimpleDateFormat sdf = new SimpleDateFormat(getContext().getString(R.string.datetime_format_yyyy_mm_dd_t_hh_mm_ss), Locale.getDefault());

                            sdf.setTimeZone(TimeZone.getDefault());

                            try {
                                date = sdf.parse(closedFrom);
                                closedFrom = sdf.format(date);
                                closedFrom = getString(R.string.closed_from_date, closedFrom.replace('T', ' ').substring(0, closedFrom.length() - 3));
                                date = sdf.parse(closedTo);
                                closedTo = sdf.format(date);
                                closedTo = getString(R.string.closed_to_date, closedTo.replace('T', ' ').substring(0, closedTo.length() - 3));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            bottomSheetJMessageTitleTextView.setText(name);
                            bottomSheetJMessageDescriptionTextView.setText(description);
                            bottomSheetJMessageStartDateTextView.setText(closedFrom);

                            bottomSheetJMessageClosedForTextView.setVisibility(closedFor != null ? View.VISIBLE : View.GONE);
                            bottomSheetJMessageClosedForTextView.setText(closedFor);
                            bottomSheetJMessageFishGroupTextView.setText(fishGroup);
                            bottomSheetJMessageAreaTextView.setText(area);
                            bottomSheetJMessageAreaTextView.setTypeface(null, Typeface.NORMAL);
                            bottomSheetJMessageLinksTitleTextView.setText(R.string.links);
//                        bottomSheetJMessageLastUpdatedTextView.setText(closedFor);
                            bottomSheetJMessageLastUpdatedTextView.setVisibility(View.GONE);
                            bottomSheetJMessageFisheryMessagesLinkTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_insert_link_black_24dp, 0 ,0 ,0);

                            String DataSourceUrl = "https://www.barentswatch.no/om/Partnere/ff/Fiskeridirektoratet/";
                            String fisheryMessagesUrl = "https://www.fiskeridir.no/Yrkesfiske/Regelverk-og-reguleringer/Fiskerimeldinger";
                            String jMessagesInformationUrl = "https://www.fiskeridir.no/Yrkesfiske/Regelverk-og-reguleringer/J-meldinger/Gjeldende-J-meldinger/";

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                bottomSheetJMessageVersionTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(jMessagesUrl, version), Html.FROM_HTML_MODE_LEGACY));
                                bottomSheetJMessageFisheryMessagesLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(fisheryMessagesUrl, getString(R.string.fishery_messages_fishery_directorate)), Html.FROM_HTML_MODE_LEGACY));
                                bottomSheetJMessageJMessagesLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(jMessagesInformationUrl, getString(R.string.j_messages_fishery_directorate)), Html.FROM_HTML_MODE_LEGACY));
                                bottomSheetJMessageDataSourceTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(DataSourceUrl, getString(R.string.data_source_fishery_directorate)), Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                bottomSheetJMessageVersionTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(jMessagesUrl, version)));
                                bottomSheetJMessageFisheryMessagesLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(fisheryMessagesUrl, getString(R.string.fishery_messages_fishery_directorate))));
                                bottomSheetJMessageJMessagesLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(jMessagesInformationUrl, getString(R.string.j_messages_fishery_directorate))));
                                bottomSheetJMessageDataSourceTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(DataSourceUrl, getString(R.string.data_source_fishery_directorate))));
                            }

                            bottomSheetJMessageVersionTextView.setMovementMethod(LinkMovementMethod.getInstance());
                            bottomSheetJMessageFisheryMessagesLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
                            bottomSheetJMessageJMessagesLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
                            bottomSheetJMessageDataSourceTextView.setMovementMethod(LinkMovementMethod.getInstance());

                            bottomSheetJMessageLinksLinearLayout.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDataSourcesLinearLayout.setVisibility(View.VISIBLE);
                            bottomSheetJMessageTitleTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDescriptionTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageStartDateTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageClosedForTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageFishGroupTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageAreaTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageVersionTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageFisheryMessagesLinkTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageJMessagesLinkTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDataSourceTextView.setVisibility(View.VISIBLE);
                        } else if(finalJsonObject.getJSONObject("properties").has("start_point_description")) {
                            String name = getString(R.string.fjord_lines_coast_cod);
                            String startPoint = finalJsonObject.getJSONObject("properties").getString("start_point_description");
                            String endPoint = finalJsonObject.getJSONObject("properties").getString("end_point_description");
                            String description = startPoint + " til " + endPoint;

                            bottomSheetJMessageTitleTextView.setText(name);
                            bottomSheetJMessageDescriptionTextView.setText(description);
                            bottomSheetJMessageAreaTextView.setText(R.string.click_to_read_more);
                            bottomSheetJMessageAreaTextView.setTypeface(null, Typeface.BOLD);
                            bottomSheetJMessageFjordLinesDetailsTextView.setText(R.string.coast_cod_regulation_details);
                            bottomSheetJMessageFisheryMessagesLinkTextView.setText(R.string.fjord_lines_important_information);
                            bottomSheetJMessageFisheryMessagesLinkTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            bottomSheetJMessageLinksTitleTextView.setText(R.string.important);

                            bottomSheetJMessageAreaTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(bottomSheetJMessageAreaTextView.getText().equals(getString(R.string.click_to_read_more))) {
                                        bottomSheetJMessageAreaTextView.setText(R.string.click_to_hide);
                                        bottomSheetJMessageFjordLinesDetailsScrollView.setVisibility(View.VISIBLE);
                                        bottomSheetJMessageVersionTextView.setVisibility(View.VISIBLE);
                                        bottomSheetJMessageTitleTextView.setVisibility(View.GONE);
                                        bottomSheetJMessageDescriptionTextView.setVisibility(View.GONE);
                                    } else {
                                        bottomSheetJMessageAreaTextView.setText(R.string.click_to_read_more);
                                        bottomSheetJMessageAreaTextView.setTypeface(null, Typeface.BOLD);
                                        bottomSheetJMessageFjordLinesDetailsScrollView.setVisibility(View.GONE);
                                        bottomSheetJMessageTitleTextView.setVisibility(View.VISIBLE);
                                        bottomSheetJMessageDescriptionTextView.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            bottomSheetJMessageFjordLinesDetailsScrollView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                        bottomSheetJMessageAreaTextView.setText(R.string.click_to_read_more);
                                        bottomSheetJMessageAreaTextView.setTypeface(null, Typeface.BOLD);
                                        bottomSheetJMessageFjordLinesDetailsScrollView.setVisibility(View.GONE);
                                        bottomSheetJMessageTitleTextView.setVisibility(View.GONE);
                                        bottomSheetJMessageDescriptionTextView.setVisibility(View.GONE);
                                }
                            });

//                            bottomSheetJMessageFisheryMessagesLinkTextView.setVisibility(View.GONE);
                            bottomSheetJMessageLinksLinearLayout.setVisibility(View.VISIBLE);
                            bottomSheetJMessageTitleTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDescriptionTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDataSourceTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDataSourcesLinearLayout.setVisibility(View.VISIBLE);
                            bottomSheetJMessageAreaTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageVersionTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageLastUpdatedTextView.setVisibility(View.GONE);
                            bottomSheetJMessageJMessagesLinkTextView.setVisibility(View.GONE);

                            String regulationDetailsLink = "http://www.fiskeridir.no/Yrkesfiske/Regelverk-og-reguleringer/J-meldinger/Gjeldende-J-meldinger/J-125-2016";
                            String DataSourceUrl = "https://www.barentswatch.no/om/Partnere/ff/Fiskeridirektoratet/";

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                bottomSheetJMessageDataSourceTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(DataSourceUrl, getString(R.string.data_source_fishery_directorate)), Html.FROM_HTML_MODE_LEGACY));
                                bottomSheetJMessageVersionTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(regulationDetailsLink, getString(R.string.fishery_directory_url)), Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                bottomSheetJMessageDataSourceTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(DataSourceUrl, getString(R.string.data_source_fishery_directorate))));
                                bottomSheetJMessageVersionTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(regulationDetailsLink, getString(R.string.fishery_directory_url))));
                            }

                            bottomSheetJMessageDataSourceTextView.setMovementMethod(LinkMovementMethod.getInstance());


                        } else if(finalJsonObject.getJSONObject("properties").has("type_vern")) {
                            String name = finalJsonObject.getJSONObject("properties").getString("navn");
                            String species = finalJsonObject.getJSONObject("properties").getString("art");
                            String protectionType = finalJsonObject.getJSONObject("properties").getString("type_vern");
                            String law = finalJsonObject.getJSONObject("properties").getString("lov");
                            String description = protectionType + " - " + species + "\n\n§ - " + law;

                            bottomSheetJMessageTitleTextView.setText(name);
                            bottomSheetJMessageDescriptionTextView.setText(description);

                            bottomSheetJMessageTitleTextView.setVisibility(View.VISIBLE);
                            bottomSheetJMessageDescriptionTextView.setVisibility(View.VISIBLE);

                            String DataSourceUrl = "https://www.barentswatch.no/om/Partnere/ff/Fiskeridirektoratet/";

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                bottomSheetJMessageDataSourceTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(DataSourceUrl, getString(R.string.data_source_fishery_directorate)), Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                bottomSheetJMessageDataSourceTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(DataSourceUrl, getString(R.string.data_source_fishery_directorate))));
                            }

                            bottomSheetJMessageDataSourceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    bottomSheetBehavior.setPeekHeight(200);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void updateSeismicBottomSheet(final String jsonString) {

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(jsonObject == null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }

            final JSONObject finalJsonObject = jsonObject;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomSheetToolLayout.setVisibility(View.GONE);
                    bottomSheetSeaFloorInstallationLayout.setVisibility(View.GONE);
                    bottomSheetIceConcentrationLayout.setVisibility(View.GONE);
                    bottomSheetJMessageLayout.setVisibility(View.GONE);
                    bottomSheetInformationContainer.setVisibility(View.GONE);
                    bottomSheetSeismicLayout.setVisibility(View.VISIBLE);

                    try {
                        JSONArray toolJsonCoordinates = finalJsonObject.getJSONObject("geometry").getJSONArray("coordinates");
                        String fromDateString = finalJsonObject.getJSONObject("properties").getString("plnfrmdate");
                        String toDateString = finalJsonObject.getJSONObject("properties").getString("plntodate");
                        String vesselName = finalJsonObject.getJSONObject("properties").getString("vesselall");
                        String company = finalJsonObject.getJSONObject("properties").getString("compreport");
                        String factUrl = finalJsonObject.getJSONObject("properties").getString("factv2url");
                        String mapUrl = finalJsonObject.getJSONObject("properties").getString("mapurl");
                        Date date;

                        fromDateString = (fromDateString != null && fromDateString.length() > 10) ? fromDateString.substring(0, 10) : fromDateString;
                        toDateString = (toDateString != null && toDateString.length() > 10) ? toDateString.substring(0, 10) : toDateString;

                        SimpleDateFormat sdf = new SimpleDateFormat(getContext().getString(R.string.datetime_format_yyyy_mm_dd), Locale.getDefault());

                        sdf.setTimeZone(TimeZone.getDefault());

                        try {
                            date = sdf.parse(fromDateString);
                            fromDateString = sdf.format(date);
                            date = sdf.parse(toDateString);
                            toDateString = sdf.format(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(finalJsonObject.getString("fid").contains("npdsurveyongoing")) {
                            String startDate = finalJsonObject.getJSONObject("properties").getString("dtstart") != null ? finalJsonObject.getJSONObject("properties").getString("dtstart").substring(0, 10) : getString(R.string.not_available);
                            bottomSheetSeismicAreaTypeTextView.setText(R.string.seismic_survey_ongoing);
                            bottomSheetSeismicStartDateTextView.setText(getString(R.string.seismic_area_start_date) + ": " + startDate);
                            bottomSheetSeismicStartDateTextView.setVisibility(View.VISIBLE);

                        } else {
                            bottomSheetSeismicAreaTypeTextView.setText(R.string.seismic_survey_planned);
                            bottomSheetSeismicStartDateTextView.setVisibility(View.GONE);
                        }

                        bottomSheetSeismicPeriodTextView.setText(getString(R.string.seismic_period) + ": " + fromDateString + " - " + toDateString);
                        bottomSheetSeismicVesselTextView.setText(vesselName);
                        bottomSheetSeismicCompanyTextView.setText(company);
                        bottomSheetSeismicTypeTextView.setText(finalJsonObject.getJSONObject("properties").getString("surmaintyp"));
                        // TODO: Implement or link to web client?
                        bottomSheetSeismicAisTrackingTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new AlertDialog.Builder(view.getContext())
                                        .setIcon(R.drawable.ic_warning_black_24dp)
                                        .setTitle(getString(R.string.ais_tracking))
                                        .setMessage(getString(R.string.seismic_ais_tracking_not_implemented_yet))
                                        .setPositiveButton(getString(R.string.ok), null)
                                        .show();
                            }
                        });

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bottomSheetSeismicFactLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(factUrl, getString(R.string.seismic_fact_hyperlink_text)), Html.FROM_HTML_MODE_LEGACY));
                            bottomSheetSeismicMapLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(mapUrl, getString(R.string.seismic_map_hyperlink_text)), Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            bottomSheetSeismicFactLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(factUrl, getString(R.string.seismic_fact_hyperlink_text))));
                            bottomSheetSeismicMapLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(mapUrl, getString(R.string.seismic_map_hyperlink_text))));
                        }

                        bottomSheetSeismicFactLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
                        bottomSheetSeismicMapLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    bottomSheetBehavior.setPeekHeight(200);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }
            });
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void updateSeaFloorInstallationBottomSheet(final String jsonString) {

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(jsonObject == null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                return;
            }

            final JSONObject finalJsonObject = jsonObject;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomSheetToolLayout.setVisibility(View.GONE);
                    bottomSheetSeismicLayout.setVisibility(View.GONE);
                    bottomSheetIceConcentrationLayout.setVisibility(View.GONE);
                    bottomSheetJMessageLayout.setVisibility(View.GONE);
                    bottomSheetInformationContainer.setVisibility(View.GONE);
                    bottomSheetSeaFloorInstallationLayout.setVisibility(View.VISIBLE);

                    try {
                        JSONArray toolJsonCoordinates = finalJsonObject.getJSONObject("geometry").getJSONArray("coordinates");
                        String dateString = finalJsonObject.getJSONObject("properties").getString("dtstartup");
                        String name = finalJsonObject.getJSONObject("properties").getString("facname");
                        String operatorName = finalJsonObject.getJSONObject("properties").getString("curopernam");
                        String type = finalJsonObject.getJSONObject("properties").getString("fackind");
                        String function = finalJsonObject.getJSONObject("properties").getString("facfunc");
                        String depth = finalJsonObject.getJSONObject("properties").getString("waterdepth");
                        String fieldName = finalJsonObject.getJSONObject("properties").getString("belong2nm");
                        String dmsPosition;
                        String marinogramUrl;
                        Date date;

                        dateString = (dateString != null && dateString.length() > 10) ? dateString.substring(0, 10) : getString(R.string.not_available);

                        SimpleDateFormat sdf = new SimpleDateFormat(getContext().getString(R.string.datetime_format_yyyy_mm_dd), Locale.getDefault());

                        sdf.setTimeZone(TimeZone.getDefault());

                        if(!dateString.equals(getString(R.string.not_available))) {
                            try {
                                date = sdf.parse(dateString);
                                dateString = sdf.format(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        marinogramUrl = "http://www.yr.no/sted/hav/" + String.format(Locale.ENGLISH , "%.8f", toolJsonCoordinates.getDouble(1)) + "_" +
                                String.format(Locale.ENGLISH , "%.8f", toolJsonCoordinates.getDouble(0));
                        dmsPosition = FiskInfoUtility.decimalToDMS(toolJsonCoordinates.getDouble(1));
                        dmsPosition += " " + FiskInfoUtility.decimalToDMS(toolJsonCoordinates.getDouble(0));

                        bottomSheetSeaFloorInstallationNameTextView.setText(name + " - " + getString(R.string.sea_floor_installation));
                        bottomSheetSeaFloorInstallationTypeTextView.setText(getString(R.string.type) + ": " + type);
                        bottomSheetSeaFloorInstallationFunctionTextView.setText(getString(R.string.function) + ": " + function);
                        bottomSheetSeaFloorInstallationDepthTextView.setText(getString(R.string.depth) + ": " + depth);
                        bottomSheetSeaFloorInstallationFieldTextView.setText(getString(R.string.field) + ": " + fieldName);
                        bottomSheetSeaFloorInstallationStartupDateTextView.setText(getString(R.string.startup_date) + ": " + dateString);
                        bottomSheetSeaFloorInstallationOperatorTextView.setText(getString(R.string.operator) + ": " + operatorName);
                        bottomSheetSeaFloorInstallationPositionTextView.setText(dmsPosition);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bottomSheetSeaFloorInstallationMarinogramTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(marinogramUrl, getString(R.string.show_marinogram_from_yr)), Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            bottomSheetSeaFloorInstallationMarinogramTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(marinogramUrl, getString(R.string.show_marinogram_from_yr))));
                        }

                        bottomSheetSeaFloorInstallationMarinogramTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    bottomSheetBehavior.setPeekHeight(200);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }
            });
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void updateIceConcentrationBottomSheet(final String iceType) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomSheetToolLayout.setVisibility(View.GONE);
                    bottomSheetSeismicLayout.setVisibility(View.GONE);
                    bottomSheetSeaFloorInstallationLayout.setVisibility(View.GONE);
                    bottomSheetJMessageLayout.setVisibility(View.GONE);
                    bottomSheetInformationContainer.setVisibility(View.GONE);
                    bottomSheetIceConcentrationLayout.setVisibility(View.VISIBLE);

                    String iceConcentrationType = iceType != null ? iceType : getString(R.string.unknown_ice_concentration);

                    bottomSheetIceConcentrationTypeTextView.setText(IceConcentrationType.createFromValue(iceConcentrationType).toString());

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        bottomSheetIceConcentrationSatelliteImagesLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(getString(R.string.satellite_images_of_sea_ice_url), getString(R.string.satellite_images_of_sea_ice)), Html.FROM_HTML_MODE_LEGACY));
                        bottomSheetIceConcentrationMetIceInformationTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(getString(R.string.met_ice_information_url), getString(R.string.met_ice_information)), Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        bottomSheetIceConcentrationSatelliteImagesLinkTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(getString(R.string.satellite_images_of_sea_ice_url), getString(R.string.satellite_images_of_sea_ice))));
                        bottomSheetIceConcentrationMetIceInformationTextView.setText(Html.fromHtml(FiskInfoUtility.getHyperLinkString(getString(R.string.met_ice_information_url), getString(R.string.met_ice_information))));
                    }

                    bottomSheetIceConcentrationSatelliteImagesLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    bottomSheetIceConcentrationMetIceInformationTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    bottomSheetBehavior.setPeekHeight(200);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }
            });
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public String getGeoJSONFile(String fileName) {
            String directoryFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/FiskInfo/Offline/";

            File file = new File(directoryFilePath + fileName + ".JSON");
            StringBuilder jsonString = new StringBuilder();
            BufferedReader bufferReader = null;

            if(file.exists()) {
                try {
                    bufferReader = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = bufferReader.readLine()) != null) {
                        jsonString.append(line);
                        jsonString.append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bufferReader != null) {
                        try {
                            bufferReader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("Sent following layer: " + fileName);
            }

            return file.exists() ? jsonString.toString() : null;
        }

        @SuppressWarnings("unused")
        @android.webkit.JavascriptInterface
        public String getToolFeatureCollection() {
            return toolsFeatureCollection.toString();
        }
    }

    @SuppressWarnings("unused")
    private void getLayers() {
        browser.loadUrl("javascript:alert(getLayers())");
    }

    private void getLayersAndVisibility() {
        browser.loadUrl("javascript:getLayersAndState()");
    }

    private class barentswatchFiskInfoWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            List<String> layers = user.getActiveLayers();
            JSONArray json = new JSONArray(layers);

            view.loadUrl("javascript:populateMap();");
            view.loadUrl("javascript:toggleLayers(" + json + ");");

            if(toolsFeatureCollection != null && (getActivity() != null && (new FiskInfoUtility().isNetworkAvailable(getActivity())) && !user.getOfflineMode())) {
                view.loadUrl("javascript:getToolDataFromAndroid();");
            }

            pageLoaded = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    getLayersAndVisibility();
                }
            }, 3000);
        }
    }


    public class AsynchApiCallTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            BarentswatchApi barentswatchApi;
            barentswatchApi = new BarentswatchApi();
            String format = "JSON";
            Response response;

            List<PropertyDescription> subscribables;
            PropertyDescription newestSubscribable = null;
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date cachedUpdateDateTime;
            Date newestUpdateDateTime;
            SubscriptionEntry cachedEntry;
            final JSONArray toolsArray;
            final ArrayAdapter<String> adapter;
            String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/FiskInfo/Offline/";
            final List<String> vesselNames;
            byte[] data = new byte[0];
            File file = null;
            String directoryFilePath;

            cachedEntry = user.getSubscriptionCacheEntry(getString(R.string.fishing_facility_api_name));

            if(fiskInfoUtility.isNetworkAvailable(getActivity())) {
                subscribables = barentswatchApi.getApi().getSubscribable();
                for(PropertyDescription subscribable : subscribables) {
                    if(subscribable.ApiName.equals(getString(R.string.fishing_facility_api_name))) {
                        newestSubscribable = subscribable;
                        break;
                    }
                }
            } else if(cachedEntry == null){
                return false;
            }

            if(cachedEntry != null) {
                directoryFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/FiskInfo/Offline/";
                file = new File(directoryFilePath + cachedEntry.mSubscribable.Name + ".JSON");
            }

            if(isCancelled()) {
                cancel(true);
            }

            if(cachedEntry != null && file.exists() && ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    cachedUpdateDateTime = simpleDateFormat.parse(cachedEntry.mLastUpdated.equals(getActivity().getString(R.string.abbreviation_na)) ? "2000-00-00T00:00:00" : cachedEntry.mLastUpdated);
                    newestUpdateDateTime = simpleDateFormat.parse(newestSubscribable != null ? newestSubscribable.LastUpdated : "2000-00-00T00:00:00");

                    if(newestSubscribable != null && cachedUpdateDateTime.getTime() - newestUpdateDateTime.getTime() < 0) {
                        response = barentswatchApi.getApi().geoDataDownload(newestSubscribable.ApiName, format);
                        try {
                            data = FiskInfoUtility.toByteArray(response.getBody().in());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(getContext() == null) {
                            cancel(true);
                        }
                        if (ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            if(new FiskInfoUtility().writeMapLayerToExternalStorage(getActivity(), data, newestSubscribable.Name.replace(",", "").replace(" ", "_"), format, downloadPath, false)) {
                                SubscriptionEntry entry = new SubscriptionEntry(newestSubscribable, true);
                                entry.mLastUpdated = newestSubscribable.LastUpdated;
                                user.setSubscriptionCacheEntry(newestSubscribable.ApiName, entry);
                                user.writeToSharedPref(getActivity());
                            }
                        }

                    } else {
                        StringBuilder jsonString = new StringBuilder();
                        BufferedReader bufferReader = null;

                        try {
                            bufferReader = new BufferedReader(new FileReader(file));
                            String line;

                            while ((line = bufferReader.readLine()) != null) {
                                jsonString.append(line);
                                jsonString.append('\n');
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if(bufferReader != null) {
                                try {
                                    bufferReader.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        data = jsonString.toString().getBytes();
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(FRAGMENT_TAG, "Invalid datetime provided");
                }
            } else if(fiskInfoUtility.isNetworkAvailable(getActivity())) {
                response = barentswatchApi.getApi().geoDataDownload(newestSubscribable.ApiName, format);
                try {
                    data = FiskInfoUtility.toByteArray(response.getBody().in());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if(new FiskInfoUtility().writeMapLayerToExternalStorage(getActivity(), data, newestSubscribable.Name.replace(",", "").replace(" ", "_"), format, downloadPath, false)) {
                        SubscriptionEntry entry = new SubscriptionEntry(newestSubscribable, true);
                        entry.mLastUpdated = newestSubscribable.LastUpdated;
                        user.setSubscriptionCacheEntry(newestSubscribable.ApiName, entry);
                        user.writeToSharedPref(getActivity());
                    }
                }else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.drawable.ic_warning_black_24dp)
                                    .setTitle(getString(R.string.request_write_access_dialog_title))
                                    .setMessage(getString(R.string.request_write_access_map_rationale))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // After click on Ok, request the permission.
                                            requestPermissions(new String[] { WRITE_EXTERNAL_STORAGE }, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show();
                        }
                    });
                }
            } else {
                return false;
            }

            try {
                toolsFeatureCollection = new JSONObject(new String(data));
                toolsArray = toolsFeatureCollection.getJSONArray("features");
                vesselNames = new ArrayList<>();
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, vesselNames);

                for (int i = 0; i < toolsArray.length(); i++) {
                    JSONObject feature = toolsArray.getJSONObject(i);
                    String vesselName = (feature.getJSONObject("properties").getString("vesselname") != null && !feature.getJSONObject("properties").getString("vesselname").equals("null")) ? feature.getJSONObject("properties").getString("vesselname") : getString(R.string.vessel_name_unknown);
                    List<Integer> toolsIdList = vesselToolIdsMap.get(vesselName) != null ? vesselToolIdsMap.get(vesselName) : new ArrayList<Integer>();

                    if (vesselName != null && !vesselNames.contains(vesselName)) {
                        vesselNames.add(vesselName);
                    }

                    toolsIdList.add(i);
                    vesselToolIdsMap.put(vesselName, toolsIdList);
                    toolMap.put(feature.getJSONObject("properties").getString("toolid"), feature);
                }
            } catch (JSONException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(getContext())
                                .setTitle(getString(R.string.search_tools_init_error))
                                .setMessage(getString(R.string.search_tools_init_info))
                                .setPositiveButton(getString(R.string.ok), null)
                                .show();
                    }
                });
                Log.e(FRAGMENT_TAG, "JSON parse error");
                e.printStackTrace();

                return false;
            }


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchEditText.setVisibility(View.VISIBLE);
                    searchEditText.setAdapter(adapter);

                    searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String vesselName = ((TextView) view).getText().toString();
                            highlightVesselInMap(vesselName);

                            searchEditText.setTag(getString(R.string.map_search_view_tag_clear));
                            searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_black_24dp, 0);

                            InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                        }
                    });
                }
            });


            return true;
        }

        @Override
        protected void onCancelled(Boolean result) {
            Log.d("MapAsync", "Map async cancelled");
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                if(pageLoaded) {
                    browser.loadUrl("javascript:getToolDataFromAndroid();");
                }
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.tools_search_no_data_title))
                        .setMessage(getString(R.string.tools_search_no_data))
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
            }
        }
    }
    //
    private void createMapLayerSelectionDialog() {
        if(layersAndVisibility == null) {
            return;
        }

        final Dialog dialog = dialogInterface.getDialog(getActivity(), R.layout.dialog_select_map_layers, R.string.choose_map_layers);
        Button okButton = (Button) dialog.findViewById(R.id.select_map_layers_update_map_button);
        final List<CheckBoxRow> rows = new ArrayList<>();
        final LinearLayout mapLayerLayout = (LinearLayout) dialog.findViewById(R.id.map_layers_checkbox_layout);
        final Button cancelButton = (Button) dialog.findViewById(R.id.select_map_layers_cancel_button);
        LayerAndVisibility[] layers = new Gson().fromJson(layersAndVisibility.toString(), LayerAndVisibility[].class);
        for (LayerAndVisibility layer : layers) {
            if (layer.name.equals("Grunnkart") || layer.name.contains("OpenLayers_Control")) {
                continue;
            }
            boolean isActive;
            isActive = layer.isVisible;

            CheckBoxRow row = new CheckBoxRow(getActivity(), layer.name, false, isActive);
            rows.add(row);
            View mapLayerRow = row.getView();
            mapLayerLayout.addView(mapLayerRow);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> layersList = new ArrayList<>();
                layersList.add("Grunnkart");

                for (int i = 0; i < mapLayerLayout.getChildCount(); i++) {
                    if (rows.get(i).isChecked()) {
                        layersList.add(rows.get(i).getText());
                    }
                }

                user.setActiveLayers(layersList);
                user.writeToSharedPref(getActivity());
                dialog.dismiss();

                if(layersList.contains(getString(R.string.fishing_facility_name)) && !user.getIsFishingFacilityAuthenticated()) {
                    dialogInterface.getHyperlinkAlertDialog(getActivity(), getString(R.string.about_fishing_facility_title), getString(R.string.about_fishing_facility_details)).show();
                }

                JSONArray json = new JSONArray(layersList);
                browser.loadUrl("javascript:toggleLayers(" + json + ")");

                getLayersAndVisibility();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void createToolSymbolExplanationDialog() {
        final Dialog dialog = dialogInterface.getDialog(getActivity(), R.layout.dialog_tool_legend, R.string.tool_legend);

        TableLayout tableLayout = (TableLayout) dialog.findViewById(R.id.tool_legend_table_layout);
        Button dismissButton = (Button) dialog.findViewById(R.id.tool_legend_dismiss_button);

        for (ToolType toolType : ToolType.values()) {
            View toolLegendRow = new ToolLegendRow(getActivity(), toolType.getHexColorValue(), toolType.toString()).getView();
            tableLayout.addView(toolLegendRow);
        }

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void createProximityAlertSetupDialog() {
        final Dialog dialog = dialogInterface.getDialog(getActivity(), R.layout.dialog_proximity_alert_create, R.string.create_proximity_alert);

        Button setProximityAlertWatcherButton = (Button) dialog.findViewById(R.id.create_proximity_alert_create_alert_watcher_button);
        Button stopCurrentProximityAlertWatcherButton = (Button) dialog.findViewById(R.id.create_proximity_alert_stop_existing_alert_button);
        Button cancelButton = (Button) dialog.findViewById(R.id.create_proximity_alert_cancel_button);
        SeekBar seekbar = (SeekBar) dialog.findViewById(R.id.create_proximity_alert_seekBar);
        final EditText radiusEditText = (EditText) dialog.findViewById(R.id.create_proximity_alert_range_edit_text);
        final Switch formatSwitch = (Switch) dialog.findViewById(R.id.create_proximity_alert_format_switch);

        final double seekBarStepSize = (double) (getResources().getInteger(R.integer.proximity_alert_maximum_warning_range_meters) - getResources().getInteger(R.integer.proximity_alert_minimum_warning_range_meters)) / 100;

        radiusEditText.setText(String.valueOf(getResources().getInteger(R.integer.proximity_alert_minimum_warning_range_meters)));

        formatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    buttonView.setText(getString(R.string.range_format_nautical_miles));
                } else {
                    buttonView.setText(getString(R.string.range_format_meters));
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    String range = String.valueOf((int) (getResources().getInteger(R.integer.proximity_alert_minimum_warning_range_meters) + (seekBarStepSize * progress)));
                    radiusEditText.setText(range);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setProximityAlertWatcherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toastText;

                if (proximityAlertWatcher == null) {
                    toastText = getString(R.string.proximity_alert_set);
                } else {

                    toastText = getString(R.string.proximity_alert_replace);
                }

                if (proximityAlertWatcher != null) {
                    proximityAlertWatcher.cancel(true);
                }

                mGpsLocationTracker = new GpsLocationTracker(getActivity());
                double latitude, longitude;

                if (mGpsLocationTracker.canGetLocation()) {
                    latitude = mGpsLocationTracker.getLatitude();
                    cachedLat = latitude;
                    longitude = mGpsLocationTracker.getLongitude();
                    cachedLon = longitude;
                } else {
                    mGpsLocationTracker.showSettingsAlert();
                    return;
                }

                if(formatSwitch.isChecked()) {
                    cachedDistance = Double.valueOf(radiusEditText.getText().toString()) * getResources().getInteger(R.integer.meters_per_nautical_mile);
                } else {
                    cachedDistance = Double.valueOf(radiusEditText.getText().toString());
                }

                dialog.dismiss();

                Response response;

                try {
                    String apiName = "fishingfacility";
                    String format = "OLEX";
                    String filePath;
                    String fileName = "collisionCheckToolsFile";

                    response = barentswatchApi.getApi().geoDataDownload(apiName, format);

                    if (response == null) {
                        Log.d(FRAGMENT_TAG, "RESPONSE == NULL");
                        throw new InternalError();
                    }

                    if (fiskInfoUtility.isExternalStorageWritable()) {
                        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                        String directoryName = "FiskInfo";
                        filePath = directoryPath + "/" + directoryName + "/";
                        InputStream zippedInputStream = null;

                        try {
                            TypedInput responseInput = response.getBody();
                            zippedInputStream = responseInput.in();
                            zippedInputStream = new GZIPInputStream(zippedInputStream);

                            InputSource inputSource = new InputSource(zippedInputStream);
                            InputStream input = new BufferedInputStream(inputSource.getByteStream());
                            byte data[];
                            data = FiskInfoUtility.toByteArray(input);

                            InputStream inputStream = new ByteArrayInputStream(data);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            FiskInfoPolygon2D serializablePolygon2D = new FiskInfoPolygon2D();

                            String line;
                            boolean startSet = false;
                            String[] convertedLine;
                            List<Point> shape = new ArrayList<>();
                            while ((line = reader.readLine()) != null) {
                                Point currPoint = new Point();
                                if (line.length() == 0 || line.equals("")) {
                                    continue;
                                }
                                if (Character.isLetter(line.charAt(0))) {
                                    continue;
                                }

                                convertedLine = line.split("\\s+");

                                if (line.length() > 150) {
                                    Log.d(FRAGMENT_TAG, "line " + line);
                                }

                                if(convertedLine[0].startsWith("3sl")) {
                                    continue;
                                }

                                if (convertedLine[3].equalsIgnoreCase("Garnstart") && startSet) {
                                    if (shape.size() == 1) {
                                        // Point

                                        serializablePolygon2D.addPoint(shape.get(0));
                                        shape = new ArrayList<>();
                                    } else if (shape.size() == 2) {

                                        // line
                                        serializablePolygon2D.addLine(new Line(shape.get(0), shape.get(1)));
                                        shape = new ArrayList<>();
                                    } else {

                                        serializablePolygon2D.addPolygon(new Polygon(shape));
                                        shape = new ArrayList<>();
                                    }
                                    startSet = false;
                                }

                                if (convertedLine[3].equalsIgnoreCase("Garnstart") && !startSet) {
                                    double lat = Double.parseDouble(convertedLine[0]) / 60;
                                    double lon = Double.parseDouble(convertedLine[1]) / 60;
                                    currPoint.setNewPointValues(lat, lon);
                                    shape.add(currPoint);
                                    startSet = true;
                                } else if (convertedLine[3].equalsIgnoreCase("Brunsirkel")) {
                                    double lat = Double.parseDouble(convertedLine[0]) / 60;
                                    double lon = Double.parseDouble(convertedLine[1]) / 60;
                                    currPoint.setNewPointValues(lat, lon);
                                    shape.add(currPoint);
                                }
                            }

                            reader.close();
                            new FiskInfoUtility().serializeFiskInfoPolygon2D(filePath + fileName + "." + format, serializablePolygon2D);

                            tools = serializablePolygon2D;


                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Log.e(FRAGMENT_TAG, "Error when trying to serialize file.");
                            Toast error = Toast.makeText(getActivity(), "Ingen redskaper i området du definerte", Toast.LENGTH_LONG);
                            e.printStackTrace();
                            error.show();
                            return;
                        } finally {
                            try {
                                if (zippedInputStream != null) {
                                    zippedInputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(v.getContext(), R.string.download_failed, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }

                } catch (Exception e) {
                    Log.d(FRAGMENT_TAG, "Could not download tools file");
                    Toast.makeText(getActivity(), R.string.download_failed, Toast.LENGTH_LONG).show();
                }

                runScheduledAlarm(getResources().getInteger(R.integer.zero), getResources().getInteger(R.integer.proximity_alert_interval_time_seconds));

                Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
            }
        });

        if (proximityAlertWatcher != null) {
            TypedValue outValue = new TypedValue();
            stopCurrentProximityAlertWatcherButton.setVisibility(View.VISIBLE);


            getResources().getValue(R.dimen.proximity_alert_dialog_button_text_size_small, outValue, true);
            float textSize = outValue.getFloat();

            setProximityAlertWatcherButton.setTextSize(textSize);
            stopCurrentProximityAlertWatcherButton.setTextSize(textSize);
            cancelButton.setTextSize(textSize);

            stopCurrentProximityAlertWatcherButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    proximityAlertWatcher.cancel(true);
                    proximityAlertWatcher = null;
                    dialog.dismiss();
                }
            });
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void runScheduledAlarm(int initialDelay, int period) {
        proximityAlertWatcher = new FiskinfoScheduledTaskExecutor(2).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                // Need to get alarm status and handle kill
                if (!cacheDeserialized) {
                    String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                    String directoryName = "FiskInfo";
                    String filename = "collisionCheckToolsFile";
                    String format = "OLEX";
                    String filePath = directoryPath + "/" + directoryName + "/" + filename + "." + format;
                    tools = fiskInfoUtility.deserializeFiskInfoPolygon2D(filePath);
                    cacheDeserialized = true;
                } else {
                    if(alarmFiring) {
                        return;
                    }

                    double latitude, longitude;

                    if (mGpsLocationTracker.canGetLocation()) {
                        latitude = mGpsLocationTracker.getLatitude();
                        cachedLat = latitude;
                        longitude = mGpsLocationTracker.getLongitude();
                        cachedLon = longitude;
                        Log.i("GPS-LocationTracker", String.format("latitude: %s, ", latitude));
                        Log.i("GPS-LocationTracker", String.format("longitude: %s", longitude));
                    } else {
                        mGpsLocationTracker.showSettingsAlert();
                        return;
                    }
                    Point userPosition = new Point(cachedLat, cachedLon);

                    if (tools.checkCollisionWithPoint(userPosition, cachedDistance)) {
                        alarmFiring = true;
                        Looper.prepare();
                        notifyUserOfProximityAlert();
                    }
                }

                System.out.println("Collision check run");
            }

        }, initialDelay, period, TimeUnit.SECONDS);
    }

    private void notifyUserOfProximityAlert() {
        Handler mainHandler = new Handler(getActivity().getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = dialogInterface.getDialog(getActivity(), R.layout.dialog_proximity_alert_warning, R.string.proximity_alert_warning);

                Button okButton = (Button) dialog.findViewById(R.id.proximity_alert_warning_ok_button);
                Button showOnMapButton = (Button) dialog.findViewById(R.id.proximity_alert_warning_show_on_map_button);
                Button dismissAlertButton = (Button) dialog.findViewById(R.id.proximity_alert_warning_dismiss_button);

                long[] pattern = {0, 500, 200, 200, 300, 200, 200};

                vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(pattern, 0);

                mediaPlayer = MediaPlayer.create(getActivity(), R.raw.proximity_warning_sound);

                if (mediaPlayer == null) {
                    return;
                }

                mediaPlayer.setLooping(true);
                mediaPlayer.start();

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alarmFiring = false;
                        vibrator.cancel();
                        vibrator = null;
                        proximityAlertWatcher.cancel(true);
                        proximityAlertWatcher = null;
                        mediaPlayer.stop();
                        mediaPlayer.release();

                        dialog.dismiss();
                        runScheduledAlarm(getResources().getInteger(R.integer.sixty), getResources().getInteger(R.integer.proximity_alert_interval_time_seconds));
                    }
                });

                showOnMapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alarmFiring = false;
                        vibrator.cancel();
                        vibrator = null;
                        proximityAlertWatcher.cancel(true);
                        proximityAlertWatcher = null;
                        mediaPlayer.stop();
                        mediaPlayer.release();



                        browser.loadUrl("javascript:zoomToUserPosition()");

                        dialog.dismiss();
                        runScheduledAlarm(getResources().getInteger(R.integer.sixty), getResources().getInteger(R.integer.proximity_alert_interval_time_seconds));

                    }
                });

                dismissAlertButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alarmFiring = false;
                        vibrator.cancel();
                        vibrator = null;
                        proximityAlertWatcher.cancel(true);
                        proximityAlertWatcher = null;
                        mediaPlayer.stop();
                        mediaPlayer.release();

                        dialog.dismiss();
                    }
                });

                dialog.show();
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    public void updateMap() {
        pageLoaded = false;
        if((new FiskInfoUtility().isNetworkAvailable(getActivity())) && !user.getOfflineMode()) {
            browser.loadUrl("file:///android_asset/mapApplication.html");

            asynchApiCallTask = new AsynchApiCallTask();
            asynchApiCallTask.execute();
            ((MainActivity)getActivity()).toggleNetworkErrorTextView(true);
        } else {
            browser.loadUrl("file:///android_asset/mapApplicationOfflineMode.html");

            if((!new FiskInfoUtility().isNetworkAvailable(getActivity()))) {
                new AlertDialog.Builder(getContext())
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(getString(R.string.offline_mode_map_used_title))
                        .setMessage(getString(R.string.offline_mode_map_used_info))
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
            }

            populateSearchFieldFromLocalFile();
        }
    }

    private void populateSearchFieldFromLocalFile() {
        String directoryFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/FiskInfo/Offline/";

        List<String> vesselNames = new ArrayList<>();
        final JSONArray toolsArray;
        File file = new File(directoryFilePath + "Redskap" + ".JSON");
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferReader = null;

        if(file.exists()) {
            try {
                bufferReader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = bufferReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferReader != null) {
                    try {
                        bufferReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return;
        }

        try {
            toolsFeatureCollection = new JSONObject(stringBuilder.toString());
            toolsArray = toolsFeatureCollection.getJSONArray("features");

            for (int i = 0; i < toolsArray.length(); i++) {
                JSONObject feature = toolsArray.getJSONObject(i);
                String vesselName = (feature.getJSONObject("properties").getString("vesselname") != null && !feature.getJSONObject("properties").getString("vesselname").equals("null")) ? feature.getJSONObject("properties").getString("vesselname") : getString(R.string.vessel_name_unknown);
                List<Integer> toolsIdList = vesselToolIdsMap.get(vesselName) != null ? vesselToolIdsMap.get(vesselName) : new ArrayList<Integer>();

                if (vesselName != null && !vesselNames.contains(vesselName)) {
                    vesselNames.add(vesselName);
                }

                toolsIdList.add(i);
                vesselToolIdsMap.put(vesselName, toolsIdList);
                toolMap.put(feature.getJSONObject("properties").getString("toolid"), feature);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, vesselNames);
        searchEditText.setVisibility(View.VISIBLE);
        searchEditText.setAdapter(adapter);

        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String vesselName = ((TextView) view).getText().toString();
                highlightVesselInMap(vesselName);

                searchEditText.setTag(getString(R.string.map_search_view_tag_clear));
                searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_black_24dp, 0);
                searchEditText.setThreshold(1);

                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });
    }

    private void zoomToUserPosition() {
        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { ACCESS_FINE_LOCATION }, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            browser.loadUrl("javascript:zoomToUserPosition()");
        }
    }

    private void centerVesselInMap(String vesselName) {
        String mmsi = getMMSIFromVesselName(vesselName);
        browser.loadUrl("javascript:centerVessel(\"" + mmsi + "\")");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void highlightToolsInMap(String vesselName) {
        String mmsi = getMMSIFromVesselName(vesselName);
        browser.loadUrl("javascript:highlightTools(\"" + mmsi + "\")");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void highlightVesselInMap(String vesselName) {
        String mmsi = vesselName != null ? getMMSIFromVesselName(vesselName) : null;
        browser.loadUrl("javascript:highlightVessel(\"" + mmsi + "\")");
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private String getMMSIFromVesselName(String vesselName) {
        List<Integer> vesselTools = vesselToolIdsMap.get(vesselName);
        String mmsi = null;
        for(int toolId : vesselTools) {
            JSONObject tool = null;
            try {
                tool = ((JSONObject)toolsFeatureCollection.getJSONArray("features").get(toolId));
                mmsi = tool.getJSONObject("properties").getString("mmsi");
                break;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mmsi;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                for(int i = 0; i < permissions.length; i++) {
                    if(permissions[i].equals(WRITE_EXTERNAL_STORAGE) && results[i] == PackageManager.PERMISSION_GRANTED) {
                        updateMap();
                    }
                }
                break;
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                for(int i = 0; i < permissions.length; i++) {
                    if(permissions[i].equals(ACCESS_FINE_LOCATION) && results[i] == PackageManager.PERMISSION_GRANTED && browser != null) {
                        browser.loadUrl("javascript:zoomToUserPosition()");
                    } else if(permissions[i].equals(ACCESS_FINE_LOCATION) && results[i] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getContext(), R.string.error_cannot_zoom_to_position_without_location_access, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_map:
                updateMap();
                return true;
            case R.id.zoom_to_user_position:
                zoomToUserPosition();
                return true;
            case R.id.symbol_explanation:
                createToolSymbolExplanationDialog();
                return true;
            case R.id.setProximityAlert:
                createProximityAlertSetupDialog();
                return true;
            case R.id.choose_map_layers:
                createMapLayerSelectionDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}