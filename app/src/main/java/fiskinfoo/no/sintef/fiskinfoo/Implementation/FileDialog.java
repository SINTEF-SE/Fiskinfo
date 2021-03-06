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

package fiskinfoo.no.sintef.fiskinfoo.Implementation;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import fiskinfoo.no.sintef.fiskinfoo.R;

public class FileDialog extends ListActivity {

    private static final String ITEM_KEY = "key";

    private static final String ITEM_IMAGE = "image";

    private static final String ROOT = Environment.getExternalStorageDirectory().getPath(); //"/";;

    public static final String START_PATH = "START_PATH";

    public static final String FORMAT_FILTER = "FORMAT_FILTER";

    public static final String RESULT_PATH = "RESULT_PATH";

    public static final String SELECTION_MODE = "SELECTION_MODE";

    public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";

    private List<String> path = null;
    private TextView myPath;
    private EditText mFileName;
    private ArrayList<HashMap<String, Object>> mList;

    private Button selectButton;

    private LinearLayout layoutSelect;
    private LinearLayout layoutCreate;
    private InputMethodManager inputManager;
    private String parentPath;
    private String currentPath = ROOT;

    private String[] formatFilter = null;

    private boolean canSelectDir = false;

    private File selectedFile;
    private HashMap<String, Integer> lastPositions = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, getIntent());

        setContentView(R.layout.dialog_select_download_path_main);
        myPath = (TextView) findViewById(R.id.path);
        mFileName = (EditText) findViewById(R.id.fd_edit_text_file);

        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        selectButton = (Button) findViewById(R.id.fd_button_select);
        selectButton.setEnabled(false);
        selectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                File dir = new File(selectedFile.getAbsolutePath());

                if(!dir.isDirectory() || !dir.canWrite()) {
                    Toast.makeText(v.getContext(), R.string.error_invalid_directory_cannot_write, Toast.LENGTH_LONG).show();
                    return;
                }

                if (selectedFile != null) {
                    getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });

        final Button newButton = (Button) findViewById(R.id.fd_button_new);
        newButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setCreateVisible(v);

                mFileName.setText("");
                mFileName.requestFocus();
            }
        });

        int selectionMode = getIntent().getIntExtra(SELECTION_MODE, SelectionMode.MODE_CREATE);

        formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);

        canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, false);

        if (selectionMode == SelectionMode.MODE_OPEN) {
            newButton.setEnabled(false);
        }

        layoutSelect = (LinearLayout) findViewById(R.id.fd_linear_layout_select);
        layoutCreate = (LinearLayout) findViewById(R.id.fd_linear_layout_create);
        layoutCreate.setVisibility(View.GONE);

        final Button cancelCreateButton = (Button) findViewById(R.id.fd_button_cancel_create);
        cancelCreateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setSelectVisible(v);
            }

        });
        final Button createButton = (Button) findViewById(R.id.fd_button_create);
        createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mFileName.setError(null);
                if (mFileName.getText().length() > 0) {
                    File directory = new File(currentPath + "/" + mFileName.getText());

                    if (!(directory.exists()) && !directory.mkdirs()) {
                        Log.e("FileDialog", "Cannot create directory");
                    }

                    getIntent().putExtra(RESULT_PATH, currentPath + "/" + mFileName.getText());
                    setResult(RESULT_OK, getIntent());
                    finish();
                } else {
                    mFileName.setError(getString(R.string.no_folder_name));
                }
            }
        });

        final Button cancelButton = (Button) findViewById(R.id.fd_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
        String startPath = getIntent().getStringExtra(START_PATH);
        startPath = startPath != null ? startPath : ROOT;
        if (canSelectDir) {
            selectedFile = new File(startPath);
            selectButton.setEnabled(true);
        }
        getDir(startPath);
    }

    private void getDir(String dirPath) {

        boolean useAutoSelection = dirPath.length() < currentPath.length();

        Integer position = lastPositions.get(parentPath);

        getDirImpl(dirPath);

        if (position != null && useAutoSelection) {
            getListView().setSelection(position);
        }

    }

    private void getDirImpl(final String dirPath) {

        currentPath = dirPath;

        path = new ArrayList<>();
        mList = new ArrayList<>();

        File f = new File(currentPath);
        File[] files = f.listFiles();
        if (files == null) {
            currentPath = ROOT;
            f = new File(currentPath);
            files = f.listFiles();
        }
        myPath.setText(getText(R.string.location) + ": " + currentPath);

        if (!currentPath.equals(ROOT)) {

            addItem(ROOT, R.drawable.folder);
            path.add(ROOT);

            addItem("../", R.drawable.folder);
            path.add(f.getParent());
            parentPath = f.getParent();

        }

        TreeMap<String, String> dirsMap = new TreeMap<>();
        TreeMap<String, String> dirsPathMap = new TreeMap<>();
        TreeMap<String, String> filesMap = new TreeMap<>();
        TreeMap<String, String> filesPathMap = new TreeMap<>();
        if (files == null) {
            Toast.makeText(this, getString(R.string.file_dialog_no_folder_for_storage), Toast.LENGTH_LONG).show();
        }
        else {
            for (File file : files) {
                if (file.isDirectory()) {
                    String dirName = file.getName();
                    dirsMap.put(dirName, dirName);
                    dirsPathMap.put(dirName, file.getPath());
                } else {
                    final String fileName = file.getName();
                    final String fileNameLwr = fileName.toLowerCase();
                    if (formatFilter != null) {
                        boolean contains = false;
                        for (String aFormatFilter : formatFilter) {
                            final String formatLwr = aFormatFilter.toLowerCase();
                            if (fileNameLwr.endsWith(formatLwr)) {
                                contains = true;
                                break;
                            }
                        }
                        if (contains) {
                            filesMap.put(fileName, fileName);
                            filesPathMap.put(fileName, file.getPath());
                        }
                    } else {
                        filesMap.put(fileName, fileName);
                        filesPathMap.put(fileName, file.getPath());
                    }
                }
            }
        }
        path.addAll(dirsPathMap.tailMap("").values());
        path.addAll(filesPathMap.tailMap("").values());

        SimpleAdapter fileList = new SimpleAdapter(this, mList, R.layout.file_dialog_row, new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] { R.id.fdrowtext,
                R.id.fdrowimage });

        for (String dir : dirsMap.tailMap("").values()) {
            addItem(dir, R.drawable.folder);
        }

        for (String file : filesMap.tailMap("").values()) {
            addItem(file, R.drawable.file);
        }

        fileList.notifyDataSetChanged();

        setListAdapter(fileList);

    }

    private void addItem(String fileName, int imageId) {
        HashMap<String, Object> item = new HashMap<>();
        item.put(ITEM_KEY, fileName);
        item.put(ITEM_IMAGE, imageId);
        mList.add(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(path.get(position));

        setSelectVisible(v);

        if (file.isDirectory()) {
            selectButton.setEnabled(false);
            if (file.canRead()) {
                lastPositions.put(currentPath, position);
                getDir(path.get(position));
                if (canSelectDir) {
                    selectedFile = file;
                    v.setSelected(true);
                    selectButton.setEnabled(true);
                }
            } else {
                new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle("[" + file.getName() + "] " + getText(R.string.cant_read_folder))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        } else {
            selectedFile = file;
            v.setSelected(true);
            selectButton.setEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            selectButton.setEnabled(false);

            if (layoutCreate.getVisibility() == View.VISIBLE) {
                layoutCreate.setVisibility(View.GONE);
                layoutSelect.setVisibility(View.VISIBLE);
            } else {
                if (!currentPath.equals(ROOT)) {
                    getDir(parentPath);
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            }

            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void setCreateVisible(View v) {
        layoutCreate.setVisibility(View.VISIBLE);
        layoutSelect.setVisibility(View.GONE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        selectButton.setEnabled(false);
    }

    private void setSelectVisible(View v) {
        layoutCreate.setVisibility(View.GONE);
        layoutSelect.setVisibility(View.VISIBLE);

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        selectButton.setEnabled(false);
    }
}