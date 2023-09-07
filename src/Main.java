import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        boolean hasData = true;
        String after = null;

        int idx = 0;

        while(hasData) {
            idx++;
            String query = buildQuery("547", "200", after);
            String jsonData = (String) getConnection(query);
            boolean hasNext = parseHasNext(jsonData);
            
            if(hasNext) {
                after = parseAfter(jsonData);
            } else {
                hasData = false;
            }
        }

        System.out.println("GraphQL 호출 완료 : " + idx + "번 호출");
    }

    public static String parseAfter(String jsonData) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONObject dataObject = (JSONObject) jsonObject.get("data");
            JSONObject resultObject = (JSONObject) dataObject.get("search_result");
            String after = (String) resultObject.get("end_cursor");

            after = "\"" + after + "\"";

            return after;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 파싱 오류 시 null 반환
        }
    }

    private static boolean parseHasNext(String jsonData) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONObject dataObject = (JSONObject) jsonObject.get("data");
            JSONObject resultObject = (JSONObject) dataObject.get("search_result");
            boolean hasNext = (Boolean) resultObject.get("has_next");

            return hasNext;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 파싱 오류 시 기본값으로 false 반환
        }
    }

    public static String buildQuery(String display_category_id_list, String filter_id_list, String after) {
        return "{\"query\":\"fragment DefaultInput on SearchFilterValue { key type value attribute } fragment RangeInput on SearchFilterValue { key type attribute min_value { ...DecimalUnitNumber } max_value { ...DecimalUnitNumber } } fragment DecimalUnitNumber on DecimalUnitNumber { display_unit decimal is_unit_prefix number_without_decimal unit } fragment UxGoodsCardItemPart on UxGoodsCardItem { browsing_type position type image_url webp_image_url jpeg_image_url video_url log image_ratio aid uuid product_url shop_id shop_product_no shop_name title discount_rate discount_info { image_url title color } column_count catalog_product_id goods_id one_day_delivery { ...UxCommonText } has_coupon is_zpay_discount price final_price final_price_with_currency { currency decimal price_without_decimal display_currency is_currency_prefix } max_price max_price_with_currency { currency decimal price_without_decimal display_currency is_currency_prefix } is_zonly is_brand free_shipping zpay ranking sellable_status is_ad is_exclusive similar_search review_score display_review_count badge_list { image_url dark_image_url } } fragment UxCommonText on UxCommonText { text color { normal } html { normal } } fragment UxText on UxText { type text style is_html_text } fragment UxButton on UxButton { type text is_html_text style log link_url } fragment UxSearchedShop on UxSearchedShop { id name bookmark_count category category_list { doc_count key } alias_list status is_disabled is_zpay style_list typical_image_url is_saved_shop main_domain department_badge { size text text_color font_weight background_color background_opacity border { color style width radius } } } query GetSearchResult($input: SearchResultInput\\u0021) { search_result(input: $input) { end_cursor has_next filter_list { name collapse component_list { key name type ... on SearchChipButtonFilterComponent { value_list { count selected value image_url input { ...DefaultInput } } } ... on SearchBreadcrumbFilterComponent { value_list { id name order path input { ...DefaultInput } } } ... on SearchListFilterComponent { value_list { count id name path selected input { ...DefaultInput } } } ... on SearchRangeSliderFilterComponent { input { ...RangeInput } interval { ...DecimalUnitNumber } max_without_decimal min_without_decimal selected_max_without_decimal selected_min_without_decimal } ... on SearchMessageFilterComponent { text } } } ui_item_list { __typename ... on UxNoResults { type position no_results_main_title { type text is_html_text style } no_results_sub_title { type text is_html_text style } image_url aspect_ratio } ... on UxImageBannerGroup { type id aspect_ratio is_auto_rolling update_interval item_list { id image_url landing_url log } } ... on UxFullWidthImageBannerGroup { type id aspect_ratio is_auto_rolling update_interval item_list { id image_url landing_url log } } ... on UxTextTitle { type text sub_text info { title desc } } ...UxGoodsCardItemPart ... on UxGoodsGroup { type group_type id main_title { ...UxText } sub_title { ...UxText } image { type image_url aspect_ratio log link_url } more_button { ...UxButton } action_button { ...UxButton } goods_carousel { type style line_count item_column_count more_button { ...UxButton } component_list { ...UxGoodsCardItemPart } } is_ad } ... on UxShopRankingCardItem { type position shop_id ranking_shop_name: shop_name shop_typical_image_url ranking is_saved_shop variance { type value color } component_list { ...UxGoodsCardItemPart } action_button { ...UxButton } } ... on UxGoodsCarousel { type style line_count item_column_count more_button { ...UxButton } component_list { ...UxGoodsCardItemPart } } ... on UxLineWithMargin { type color height margin { top left bottom right } } ... on UxCheckButtonAndSorting { type check_button_item_list { str_id name selected image_url disabled } sorting_item_list { str_id name selected description } } ... on UxTextAndMoreButton { type position total_count main_title { ...UxText } more_button { ...UxButton } } ... on UxSearchedShopCarousel { type position searched_shop_list { ...UxSearchedShop } } ... on UxGoodsFilterList { type filter_list { key name selected selected_count } } } } }\"," +
                "\"variables\":{\"input\":{\"display_category_id_list\":[\"" + display_category_id_list + "\"]," +
                "\"page_id\":\"srp_clp_category\",\"filter_id_list\":[\"" + filter_id_list + "\"],\"after\":" + after + "}}}";
    }

    public static Object getConnection(String query) {
        String urlString = "https://api.zigzag.kr/api/2/graphql/GetSearchResult";

        String jsonData = query;

        int responseCode = -1;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.writeBytes(jsonData);
            outputStream.flush();
            outputStream.close();

            responseCode = conn.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            while (br.ready()) {
                sb.append(br.readLine());
            }
            br.close();

            String jsonBody = sb.toString();
            System.out.println("Response Data : " + jsonBody);

            conn.disconnect();

            return jsonBody;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode;
    }
}