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

import android.content.Context;
import android.view.View;

import fiskinfoo.no.sintef.fiskinfoo.Interface.UtilityRowsInterface;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.CardViewInformationRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.CheckBoxRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.RadioButtonRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.SettingsButtonRow;
import fiskinfoo.no.sintef.fiskinfoo.UtilityRows.ToolLegendRow;

public class UtilityRows implements UtilityRowsInterface {
    @Override
    public ToolLegendRow getToolLegendRow(Context context, int toolColor, String toolName) {
        return new ToolLegendRow(context, toolColor, toolName);
    }

    @Override
    public SettingsButtonRow getSettingsButtonRow(Context context, String buttonText) {
        return new SettingsButtonRow(context, buttonText);
    }

    @Override
    public SettingsButtonRow getSettingsButtonRow(Context context, String buttonText, View.OnClickListener onClickListener) {
        return new SettingsButtonRow(context, buttonText, onClickListener);
    }

    @Override
    public CheckBoxRow getCheckBoxRow(Context context, String text) {
        return new CheckBoxRow(context, text);
    }

    @Override
    public CheckBoxRow getCheckBoxRow(Context context, String text, boolean isChecked) {
        return new CheckBoxRow(context, text, isChecked);
    }

    @Override
    public CardViewInformationRow getCardViewInformationRow(Context context, String fieldName, String fieldData, boolean indentData) {
        return new CardViewInformationRow(context, fieldName, fieldData, indentData);
    }

    @Override
    public CardViewInformationRow getCardViewInformationRow(Context context, String fieldName, String fieldData, boolean indentData, int textColor) {
        return new CardViewInformationRow(context, fieldName, fieldData, indentData, textColor);
    }

    @Override
    public RadioButtonRow getRadioButtonRow(Context context, String item) {
        return new RadioButtonRow(context, item);
    }
}

