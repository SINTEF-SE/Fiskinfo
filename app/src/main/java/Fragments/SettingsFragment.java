package Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.SubscriptionEntry;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.Tool;
import fiskinfoo.no.sintef.fiskinfoo.Baseclasses.ToolType;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FileDialog;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.FiskInfoUtility;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.SelectionMode;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.User;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UserSettings;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityDialogs;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityOnClickListeners;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.UtilityRows;
import fiskinfoo.no.sintef.fiskinfoo.Interface.UtilityRowsInterface;
import fiskinfoo.no.sintef.fiskinfoo.LoginActivity;
import fiskinfoo.no.sintef.fiskinfoo.MainActivity;
import fiskinfoo.no.sintef.fiskinfoo.R;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.EditTextRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.InfoSwitchRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.OptionsButtonRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.SettingsRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.SpinnerRow;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    public static final String FRAGMENT_TAG = "SettingsFragment";

    private OnFragmentInteractionListener mListener;
    private LinearLayout linearLayout;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        linearLayout = (LinearLayout) rootView.findViewById(R.id.settings_fragment_fields_container);

        initUserDetailsRow();
        initSetDownLoadPathRow();
        initOfflineModeRow();
        initAboutRow();
        initLogoutRow();

        return rootView;
    }

    private void initOfflineModeRow() {
        final User user = mListener.getUser();
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new UtilityDialogs().getDialog(v.getContext(), R.layout.dialog_offline_mode_info, R.string.offline_mode);
                TextView textView = (TextView) dialog.findViewById(R.id.offline_mode_info_dialog_text_view);
                LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.offline_mode_info_dialog_linear_layout);
                Button okButton = (Button) dialog.findViewById(R.id.offline_mode_info_dialog_dismiss_button);
                final Switch offlineModeSwitch = (Switch) dialog.findViewById(R.id.offline_mode_info_dialog_switch);

                offlineModeSwitch.setChecked(user.getOfflineMode());

                if(user.getOfflineMode()) {
                    offlineModeSwitch.setText(v.getResources().getString(R.string.offline_mode_active));
                } else {
                    offlineModeSwitch.setText(v.getResources().getString(R.string.offline_mode_deactivated));
                }

                textView.setText(R.string.offline_mode_info);

                for (final SubscriptionEntry entry : user.getSubscriptionCacheEntries()) {
                    final InfoSwitchRow row = new InfoSwitchRow(v.getContext(), entry.mName, entry.mLastUpdated.replace("T", "\n"));

                    row.setChecked(entry.mOfflineActive);
                    row.setOnCheckedChangedListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SubscriptionEntry updateEntry = user.getSubscriptionCacheEntry(entry.mSubscribable.ApiName);
                            updateEntry.mOfflineActive = isChecked;
                            user.setSubscriptionCacheEntry(entry.mSubscribable.ApiName, updateEntry);
                            user.writeToSharedPref(buttonView.getContext());
                        }
                    });

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            row.setChecked(!row.isChecked());
                        }
                    });

                    linearLayout.addView(row.getView());
                }

                offlineModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        user.setOfflineMode(isChecked);
                        user.writeToSharedPref(buttonView.getContext());

                        if (isChecked) {
                            offlineModeSwitch.setText(R.string.offline_mode_active);
                            mListener.toggleOfflineMode(true);
                            Toast.makeText(buttonView.getContext(), R.string.offline_mode_activated, Toast.LENGTH_LONG).show();
                        } else {
                            offlineModeSwitch.setText(R.string.offline_mode_deactivated);
                            mListener.toggleOfflineMode(false);
                            Toast.makeText(buttonView.getContext(), R.string.offline_mode_deactivated, Toast.LENGTH_LONG).show();
                        }
                    }
                });

                okButton.setOnClickListener(new UtilityOnClickListeners().getDismissDialogListener(dialog));

                dialog.show();
            }
        };

        SettingsRow row = new SettingsRow(getContext(), getString(R.string.offline_mode), R.drawable.ic_portable_wifi_off_black_24dp, onClickListener);
        linearLayout.addView(row.getView());
    }

    private void initSetDownLoadPathRow() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());

                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_CREATE);

                startActivityForResult(intent, 1);
            }
        };

        SettingsRow row = new SettingsRow(getContext(), getString(R.string.set_download_path), R.drawable.ic_description_black_24dp, onClickListener);
        linearLayout.addView(row.getView());
    }

    private void initUserDetailsRow() {
        final User user = mListener.getUser();
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final fiskinfoo.no.sintef.fiskinfoo.Interface.DialogInterface dialogInterface = new UtilityDialogs();
                final Dialog dialog = dialogInterface.getDialog(v.getContext(), R.layout.dialog_user_settings, R.string.user_settings);
                final UserSettings settings = user.getSettings() != null ? user.getSettings() : new UserSettings();

                final Button saveSettingsButton = (Button) dialog.findViewById(R.id.user_settings_save_settings_button);
                final Button cancelButton = (Button) dialog.findViewById(R.id.dialog_cancel_button);
                final LinearLayout fieldContainer = (LinearLayout) dialog.findViewById(R.id.dialog_user_settings_main_container);

                final EditTextRow contactPersonNameRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.contact_person_name), v.getContext().getString(R.string.contact_person_name));
                final EditTextRow contactPersonPhoneRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.contact_person_phone), v.getContext().getString(R.string.contact_person_phone));
                final EditTextRow contactPersonEmailRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.contact_person_email), v.getContext().getString(R.string.contact_person_email));

                final SpinnerRow toolRow = new SpinnerRow(v.getContext(), v.getContext().getString(R.string.tool_type), ToolType.getValues());
                final EditTextRow vesselNameRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.vessel_name), v.getContext().getString(R.string.vessel_name));
                final EditTextRow vesselPhoneNumberRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.vessel_phone_number), v.getContext().getString(R.string.vessel_phone_number));
                final EditTextRow vesselIrcsNumberRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.ircs_number), v.getContext().getString(R.string.ircs_number));
                final EditTextRow vesselMmsiNumberRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.mmsi_number), v.getContext().getString(R.string.mmsi_number));
                final EditTextRow vesselImoNumberRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.imo_number), v.getContext().getString(R.string.imo_number));
                final EditTextRow vesselRegistrationNumberRow = new EditTextRow(v.getContext(), v.getContext().getString(R.string.registration_number), v.getContext().getString(R.string.registration_number));

                contactPersonNameRow.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                contactPersonPhoneRow.setInputType(InputType.TYPE_CLASS_PHONE);
                contactPersonEmailRow.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                contactPersonNameRow.setHelpText(v.getContext().getString(R.string.contact_person_name_help_description));
                contactPersonPhoneRow.setHelpText(v.getContext().getString(R.string.contact_person_phone_help_description));
                contactPersonEmailRow.setHelpText(v.getContext().getString(R.string.contact_person_email_help_description));

                vesselNameRow.setInputType(InputType.TYPE_CLASS_TEXT);
                vesselPhoneNumberRow.setInputType(InputType.TYPE_CLASS_PHONE);

                vesselIrcsNumberRow.setInputType(InputType.TYPE_CLASS_TEXT);
                vesselIrcsNumberRow.setInputFilters(new InputFilter[] { new InputFilter.LengthFilter(v.getContext().getResources().getInteger(R.integer.input_length_ircs)), new InputFilter.AllCaps()});
                vesselIrcsNumberRow.setHelpText(v.getContext().getString(R.string.ircs_help_description));

                vesselMmsiNumberRow.setInputType(InputType.TYPE_CLASS_NUMBER);
                vesselMmsiNumberRow.setInputFilters(new InputFilter[] { new InputFilter.LengthFilter(v.getContext().getResources().getInteger(R.integer.input_length_mmsi))});
                vesselMmsiNumberRow.setHelpText(v.getContext().getString(R.string.mmsi_help_description));

                vesselImoNumberRow.setInputType(InputType.TYPE_CLASS_NUMBER);
                vesselImoNumberRow.setInputFilters(new InputFilter[] { new InputFilter.LengthFilter(v.getContext().getResources().getInteger(R.integer.input_length_imo))});
                vesselImoNumberRow.setHelpText(v.getContext().getString(R.string.imo_help_description));

                vesselRegistrationNumberRow.setInputType(InputType.TYPE_CLASS_TEXT);
                vesselRegistrationNumberRow.setInputFilters(new InputFilter[] { new InputFilter.LengthFilter(v.getContext().getResources().getInteger(R.integer.input_length_registration_number)), new InputFilter.AllCaps()});
                vesselRegistrationNumberRow.setHelpText(v.getContext().getString(R.string.registration_number_help_description));

                fieldContainer.addView(contactPersonNameRow.getView());
                fieldContainer.addView(contactPersonPhoneRow.getView());
                fieldContainer.addView(contactPersonEmailRow.getView());
                fieldContainer.addView(vesselNameRow.getView());
                fieldContainer.addView(toolRow.getView());
                fieldContainer.addView(vesselPhoneNumberRow.getView());
                fieldContainer.addView(vesselIrcsNumberRow.getView());
                fieldContainer.addView(vesselMmsiNumberRow.getView());
                fieldContainer.addView(vesselImoNumberRow.getView());
                fieldContainer.addView(vesselRegistrationNumberRow.getView());

                if (settings != null) {
                    ArrayAdapter<String> currentAdapter = toolRow.getAdapter();
                    toolRow.setSelectedSpinnerItem(currentAdapter.getPosition(settings.getToolType() != null ? settings.getToolType().toString() : Tool.BUNNTRÅL.toString()));
                    contactPersonNameRow.setText(settings.getContactPersonName());
                    contactPersonPhoneRow.setText(settings.getContactPersonPhone());
                    contactPersonEmailRow.setText(settings.getContactPersonEmail());
                    vesselNameRow.setText(settings.getVesselName());
                    vesselPhoneNumberRow.setText(settings.getVesselPhone());
                    vesselIrcsNumberRow.setText(settings.getIrcs());
                    vesselMmsiNumberRow.setText(settings.getMmsi());
                    vesselImoNumberRow.setText(settings.getImo());
                    vesselRegistrationNumberRow.setText(settings.getRegistrationNumber());
                }

                saveSettingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        FiskInfoUtility utility = new FiskInfoUtility();

                        boolean validated = false;

                        validated = utility.validateName(contactPersonNameRow.getFieldText().trim()) || contactPersonNameRow.getFieldText().trim().equals("");
                        contactPersonNameRow.setError(validated ? null : v.getContext().getString(R.string.error_invalid_name));
                        if(!validated) {
                            ((ScrollView)fieldContainer.getParent().getParent()).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView)fieldContainer.getParent().getParent()).scrollTo(0, contactPersonNameRow.getView().getBottom());
                                    contactPersonNameRow.requestFocus();
                                }
                            });

                            return;
                        }

                        validated = utility.validatePhoneNumber(contactPersonPhoneRow.getFieldText().trim()) || contactPersonPhoneRow.getFieldText().trim().equals("");
                        contactPersonPhoneRow.setError(validated ? null : v.getContext().getString(R.string.error_invalid_phone_number));
                        if(!validated) {
                            ((ScrollView)fieldContainer.getParent().getParent()).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView)fieldContainer.getParent().getParent()).scrollTo(0, contactPersonPhoneRow.getView().getBottom());
                                    contactPersonPhoneRow.requestFocus();
                                }
                            });

                            return;
                        }

                        validated = utility.isEmailValid(contactPersonEmailRow.getFieldText().trim()) || contactPersonEmailRow.getFieldText().trim().equals("");
                        contactPersonEmailRow.setError(validated ? null : v.getContext().getString(R.string.error_invalid_email));
                        if(!validated) {
                            ((ScrollView)fieldContainer.getParent().getParent()).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView)fieldContainer.getParent().getParent()).scrollTo(0, contactPersonEmailRow.getView().getBottom());
                                    contactPersonEmailRow.requestFocus();
                                }
                            });

                            return;
                        }

                        validated = utility.validateIRCS(vesselIrcsNumberRow.getFieldText().trim()) || vesselIrcsNumberRow.getFieldText().trim().equals("");
                        vesselIrcsNumberRow.setError(validated ? null : v.getContext().getString(R.string.error_invalid_ircs));
                        if(!validated) {
                            ((ScrollView)fieldContainer.getParent().getParent()).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView)fieldContainer.getParent().getParent()).scrollTo(0, vesselIrcsNumberRow.getView().getBottom());
                                    vesselIrcsNumberRow.requestFocus();
                                }
                            });

                            return;
                        }

                        validated = utility.validateMMSI(vesselMmsiNumberRow.getFieldText().trim()) || vesselMmsiNumberRow.getFieldText().trim().equals("");
                        vesselMmsiNumberRow.setError(validated ? null : v.getContext().getString(R.string.error_invalid_mmsi));
                        if(!validated) {
                            ((ScrollView)fieldContainer.getParent().getParent()).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView)fieldContainer.getParent().getParent()).scrollTo(0, vesselMmsiNumberRow.getView().getBottom());
                                    vesselMmsiNumberRow.requestFocus();
                                }
                            });

                            return;
                        }

                        validated = utility.validateIMO(vesselImoNumberRow.getFieldText().trim()) || vesselImoNumberRow.getFieldText().trim().equals("");
                        vesselImoNumberRow.setError(validated ? null : v.getContext().getString(R.string.error_invalid_imo));
                        if(!validated) {
                            ((ScrollView)fieldContainer.getParent().getParent()).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView)fieldContainer.getParent().getParent()).scrollTo(0, vesselImoNumberRow.getView().getBottom());
                                    vesselImoNumberRow.requestFocus();
                                }
                            });

                            return;
                        }

                        settings.setToolType(ToolType.createFromValue(toolRow.getCurrentSpinnerItem()));
                        settings.setVesselName(vesselNameRow.getFieldText().trim());
                        settings.setVesselPhone(vesselPhoneNumberRow.getFieldText().trim());
                        settings.setIrcs(vesselIrcsNumberRow.getFieldText().trim());
                        settings.setMmsi(vesselMmsiNumberRow.getFieldText().trim());
                        settings.setImo(vesselImoNumberRow.getFieldText().trim());
                        settings.setRegistrationNumber(vesselRegistrationNumberRow.getFieldText().trim());
                        settings.setContactPersonEmail(contactPersonEmailRow.getFieldText().toLowerCase().trim());
                        settings.setContactPersonName(contactPersonNameRow.getFieldText().trim());
                        settings.setContactPersonPhone(contactPersonPhoneRow.getFieldText().trim());

                        user.setSettings(settings);
                        user.writeToSharedPref(v.getContext()); //Need wait ? Let's find out

                        dialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        };

        SettingsRow row = new SettingsRow(getContext(), getString(R.string.user_information), R.drawable.ic_account_circle_black_24dp, onClickListener);
        linearLayout.addView(row.getView());
    }

    private void initLogoutRow() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setIcon(R.drawable.ic_warning_black_24dp)
                        .setTitle(getString(R.string.log_out))
                        .setMessage(getString(R.string.confirm_log_out))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                logoutUser();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        };

        SettingsRow row = new SettingsRow(getContext(), getString(R.string.log_out), R.drawable.ic_power_settings_new_black_24dp, onClickListener);
        linearLayout.addView(row.getView());
    }

    private void initAboutRow() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                AboutFragment fragment = AboutFragment.newInstance();

                fragmentManager.beginTransaction()
                        .replace(R.id.main_activity_fragment_container, fragment)
                        .addToBackStack(getString(R.string.about))
                        .commit();
            }
        };

        SettingsRow row = new SettingsRow(getContext(), getString(R.string.about), R.drawable.ic_info_black_24dp, onClickListener);
        linearLayout.addView(row.getView());
    }


    public void logoutUser() {
        User.forgetUser(getContext());
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getView() != null) {
            getView().refreshDrawableState();
        }

        MainActivity activity = (MainActivity) getActivity();
        String title = getResources().getString(R.string.settings_fragment_title);
        activity.refreshTitle(title);
    }

    public interface OnFragmentInteractionListener {
        User getUser();

        void toggleOfflineMode(boolean offline);
    }
}