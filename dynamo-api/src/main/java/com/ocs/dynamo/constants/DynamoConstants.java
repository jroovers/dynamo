/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.constants;

import java.util.Locale;

/**
 * Various constants that are used by the Dynamo framework.
 *
 * @author bas.rutten
 */
public final class DynamoConstants {

    /**
     * CSS style for add buttons
     */
    public static final String CSS_ADD_BUTTON = "addButton";

    /**
     * CSS style for Add Filter button in flexible search layout
     */
    public static final String CSS_ADD_FILTER_BUTTON = "addFilterButton";

    /**
     * CSS style for back buttons
     */
    public static final String CSS_BACK_BUTTON = "backButton";

    /**
     * CSS style for cancel buttons
     */
    public static final String CSS_CANCEL_BUTTON = "cancelButton";

    /**
     * The CSS class that is given to an image component used to display an uploaded
     * file
     */
    public static final String CSS_CLASS_UPLOAD = "fileUpload";

    /**
     * The CSS class for the clear button
     */
    public static final String CSS_CLEAR_BUTTON = "clearButton";

    /**
     * The CSS class that indicates a dangerous value
     */
    public static final String CSS_DANGER = "danger";

    /**
     * The CSS class used to indicate a divider row in a grid
     */
    public static final String CSS_DIVIDER = "divider";

    /**
     * CSS style for button bar components
     */
    public static final String CSS_DYNAMO_BUTTON_BAR = "dynamoButtonBar";

    /**
     * CSS style for Dynamo-generated field
     */
    public static final String CSS_DYNAMO_FIELD = "dynamoField";

    /**
     * CSS style for Dynamo-generated form
     */
    public static final String CSS_DYNAMO_FORM = "dynamoForm";

    /**
     * CSS style for Dynamo-generated search form
     */
    public static final String CSS_DYNAMO_SEARCH_FORM = "dynamoSearchForm";

    /**
     * CSS style for details edit grid
     */
    public static final String CSS_DETAILS_EDIT_GRID = "detailsEditGrid";
    
    /**
     * The CSS class used to indicate that an element is the first child element
     */
    public static final String CSS_FIRST = "first";

    /**
     * The CSS class for an extra caption
     */
    public static final String CSS_CAPTION = "caption";

    /**
     * The CSS class for a flexible filter row
     */
    public static final String CSS_FLEX_FILTER_ROW = "dynamoFlexFilterRow";

    /**
     * The CSS class for a flexible filter row first column
     */
    public static final String CSS_FLEX_FILTER_ROW_FIRST = "dynamoFlexFilterRowFirst";

    /**
     * The CSS class for the last visited main menu item
     */
    public static final String CSS_LAST_VISITED = "lastVisited";

    /**
     * The CSS style for a main field
     */
    public static final String CSS_MAIN_FIELD = "mainField";

    /**
     * The CSS class that is assigned to numerical cells in a grid
     */
    public static final String CSS_NUMERICAL = "numerical";

    /**
     * The CSS class for popup dialogs
     */
    public static final String CSS_OCS_DIALOG = "ocsDialog";

    /**
     * The CSS class for the parent row
     */
    public static final String CSS_PARENT_ROW = "parentRow";

    /**
     * CSS style for remove buttons
     */
    public static final String CSS_REMOVE_BUTTON = "removeButton";

    /**
     * The CSS class for a field that must be marked as "required"
     */
    public static final String CSS_REQUIRED = "required";

    /**
     * The cSS class for save buttons
     */
    public static final String CSS_SAVE_BUTTON = "saveButton";

    /**
     * CSS style for search any buttons
     */
    public static final String CSS_SEARCH_ANY_BUTTON = "searchAnyButton";

    /**
     * CSS style for search button
     */
    public static final String CSS_SEARCH_BUTTON = "searchButton";

    /**
     * Currency symbol
     */
    public static final String CURRENCY_SYMBOL = "currencySymbol";

    /**
     * The locale to use in Date components (can be different from the main locale)
     */
    public static final String DATE_LOCALE = "dateLocale";

    /**
     * The default locale
     */
    public static final Locale DEFAULT_LOCALE = Locale.UK;

    public static final int HALF_COLUMNS = 6;

    /**
     * The default ID field
     */
    public static final String ID = "id";

    /**
     * Additional ID field
     */
    public static final String IDS = "ids";

    /**
     * Intermediate precision for floating point calculations
     */
    public static final int INTERMEDIATE_PRECISION = 10;

    public static final int MAX_COLUMNS = 12;

    /**
     * The default page size for the lazy query container.
     */
    public static final int PAGE_SIZE = 20;

    /**
     * The screen mode
     */
    public static final String SCREEN_MODE = "screenMode";

    /**
     * The name of the variable that keeps track of which tab is selected
     */
    public static final String SELECTED_TAB = "selectedTab";

    /**
     * Name of the system property that is used to determine if exporting of lists
     * (inside grids) is allowed
     */
    public static final String SP_ALLOW_LIST_EXPORT = "ocs.allow.list.export";

    /**
     * Indicates whether to capitalize individual words in property names
     */
    public static final String SP_CAPITALIZE_WORDS = "ocs.capitalize.words";

    /**
     * Name of the system property that is used to set the locale used for month
     * names in date components
     */
    public static final String SP_DATE_LOCALE = "ocs.default.date.locale";

    /**
     * Name of the system property that is used to determine the default decimal
     * precision
     */
    public static final String SP_DECIMAL_PRECISION = "ocs.default.decimal.precision";

    /**
     * Name of the system property that is used to determine the default currency
     * symbol
     */
    public static final String SP_DEFAULT_CURRENCY_SYMBOL = "ocs.default.currency.symbol";

    /**
     * Name of the system property that is used to determine the default date format
     */
    public static final String SP_DEFAULT_DATE_FORMAT = "ocs.default.date.format";

    /**
     * Name of the system property that is used to determine the default date/time
     * (time stamp) format
     */
    public static final String SP_DEFAULT_DATETIME_FORMAT = "ocs.default.datetime.format";

    /**
     * Name of the system property that is used to determine the default date/time
     * (timestamp) format with time zone
     */
    public static final String SP_DEFAULT_DATETIME_ZONE_FORMAT = "ocs.default.datetime.zone.format";

    /**
     * Name of the system property that is used to determine the default decimal
     * precision
     */
    public static final String SP_DEFAULT_DECIMAL_PRECISION = "ocs.default.decimal.precision";

    /**
     * Name of the system property that is used to determine the representation of
     * the value false
     */
    public static final String SP_DEFAULT_FALSE_REPRESENTATION = "ocs.default.false.representation";

    /**
     * Default width of input components
     */
    public static final String SP_DEFAULT_FIELD_WIDTH = "ocs.default.field.width";

    /**
     * Indicates the default column width (out of 12) for an edit form
     */
    public static final String SP_DEFAULT_FORM_COLUMN_WIDTH = "ocs.default.form.column.width";

    /**
     * Default form title width
     */
    public static final String SP_DEFAULT_FORM_TITLE_WIDTH = "ocs.default.form.title.width";

    /**
     * Default label width in columns (out of 12)
     */
    public static final String SP_DEFAULT_LABEL_COLUMN_WIDTH = "ocs.default.label.column.width";

    /**
     * Name of the system property that is used to determine the amount of rows in a
     * list select
     */
    public static final String SP_DEFAULT_LISTSELECT_ROWS = "ocs.default.listselect.rows";

    /**
     * Name of the system property that is used to set the default locale
     */
    public static final String SP_DEFAULT_LOCALE = "ocs.default.locale";

    /**
     * Name of the system property that is used to determine the default case
     * sensitiveness for search
     */
    public static final String SP_DEFAULT_SEARCH_CASE_SENSITIVE = "ocs.default.search.case.sensitive";

    /**
     * Name of the system property that is used to determine whether search is
     * prefix only
     */
    public static final String SP_DEFAULT_SEARCH_PREFIX_ONLY = "ocs.default.search.prefix.only";

    /**
     * Indicates the default column width (out of 12) for an edit form inside a
     * split layout
     */
    public static final String SP_DEFAULT_SPLIT_FORM_COLUMN_WIDTH = "ocs.default.split.form.column.width";

    /**
     * Name of the system property that is used to determine the amount of rows in a
     * text area
     */
    public static final String SP_DEFAULT_TEXTAREA_ROWS = "ocs.default.textarea.rows";

    /**
     * Name of the system property that is used to determine the default time format
     */
    public static final String SP_DEFAULT_TIME_FORMAT = "ocs.default.time.format";

    /**
     * Name of the system property that is used to determine the representation of
     * the value true
     */
    public static final String SP_DEFAULT_TRUE_REPRESENTATION = "ocs.default.true.representation";

    /**
     * Name of the system property that is used as the CSV escape character when
     * exporting
     */
    public static final String SP_EXPORT_CSV_ESCAPE = "ocs.export.csv.escape";

    /**
     * Name of the system property that is used as the CSV quote char when exporting
     */
    public static final String SP_EXPORT_CSV_QUOTE = "ocs.export.csv.quote";

    /**
     * Name of the system property that is used as the CSV separator when exporting
     */
    public static final String SP_EXPORT_CSV_SEPARATOR = "ocs.export.csv.separator";

    /**
     * Name of the system property that indicates the maximum number of items to
     * display in an entity lookup field in multiple select mode
     */
    public static final String SP_LOOKUP_FIELD_MAX_ITEMS = "ocs.default.lookupfield.max.items";

    /**
     * Class name for the service locator (override to create a different service
     * locator, e.g. to use a separate service locator for integration tests)
     */
    public static final String SP_SERVICE_LOCATOR_CLASS_NAME = "ocs.service.locator.classname";

    /**
     * System property that indicates whether to use the thousands grouping
     * separator in edit mode
     */
    public static final String SP_THOUSAND_GROUPING = "ocs.edit.thousands.grouping";

    /**
     * Indicates whether to use the display name as the input prompt by default
     */
    public static final String SP_USE_DEFAULT_PROMPT_VALUE = "ocs.use.default.prompt.value";

    /**
     * The name of the variable that is used to store the user
     */
    public static final String USER = "user";

    /**
     * The name of the variable that is used to store the user name in the session
     */
    public static final String USER_NAME = "userName";

    /**
     * The UTF-8 character set
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Constructor for OCSConstants.
     */
    private DynamoConstants() {
    }
}
