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

package fiskinfoo.no.sintef.fiskinfoo.UtilityRows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;

public abstract class BaseTableRow {
    private View mTableRow;

    public BaseTableRow(Context context, int layoutId) {
        TableRow tableRow = new TableRow(context);
        mTableRow = LayoutInflater.from(context).inflate(layoutId, tableRow, false);
    }

    /**
     * Return the table row as a View
     *
     * @return The table row
     */
    public View getView() {
        return mTableRow;
    }

    public Object getTag() {
        return mTableRow.getTag();
    }

    public void setTag(Object tag) {
        mTableRow.setTag(tag);
    }

    public void setVisibility(boolean visible) {
        getView().setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    protected Context getContext() {
        return mTableRow.getContext();
    }

    public abstract void setEnabled(boolean enabled);
}
