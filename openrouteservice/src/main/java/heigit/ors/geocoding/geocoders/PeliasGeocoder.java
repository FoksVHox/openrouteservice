/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.geocoding.geocoders;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Envelope;

import heigit.ors.exceptions.MissingParameterException;
import heigit.ors.util.HTTPUtility;

public class PeliasGeocoder extends AbstractGeocoder
{	
	public PeliasGeocoder(String geocodingURL, String reverseGeocodingURL, String userAgent) {
		super(geocodingURL, reverseGeocodingURL, userAgent);
	}
	
	@Override
	public GeocodingResult[] geocode(Address address, String languages, SearchBoundary searchBoundary, int limit)
			throws Exception {
		String lang = Helper.isEmpty(languages) ? "en" : languages;
		String reqParams = "/structured?";
		ArrayList<String> addrQueryList = new ArrayList<String>();
		// Now look at the address element and get the properties from it
		JSONObject json = new JSONObject(address.toString());
		Map<String, Object> addrMap = json.toMap();
		for(Map.Entry<String, Object> el : addrMap.entrySet()) {
			if(el.getKey() != null && el.getValue() != null) {
				addrQueryList.add(el.getKey() + "=" + URLEncoder.encode(GeocodingUtils.sanitizeAddress(el.getValue().toString()), "UTF-8"));
			}
		}
		if(addrQueryList.size() > 0)
			reqParams = reqParams + String.join("&", addrQueryList) + "&size=" + limit + "&lang=" + lang;
		else
			throw new MissingParameterException(GeocodingErrorCodes.INVALID_PARAMETER_VALUE, "address, neighbourhood, borough, locality, county, region, postalcode or country");
		
		// Add search boundary
		if (searchBoundary != null)
		{
			if (searchBoundary instanceof RectSearchBoundary)
			{
				RectSearchBoundary rsb = (RectSearchBoundary)searchBoundary;
				Envelope env = rsb.getRectangle();
				reqParams += "&boundary.rect.min_lat=" + env.getMinY() + "&boundary.rect.min_lon=" + env.getMinX() + "&boundary.rect.max_lat=" + env.getMaxY() + "&boundary.rect.max_lon=" + env.getMaxX();
			}
			else if (searchBoundary instanceof CircleSearchBoundary)
			{
				CircleSearchBoundary csb = (CircleSearchBoundary)searchBoundary;
				reqParams += "&boundary.circle.lat=" + csb.getLatitude() + "&boundary.circle.lon=" + csb.getLongitude() + "&boundary.circle.radius=" + csb.getRadius();
			}
		}
		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, 10000, userAgent, "UTF-8");
		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {

			return getGeocodeResults(respContent, searchBoundary);
		}
		
		return null;
	}

	@Override
	public GeocodingResult[] geocode(String address, String languages, SearchBoundary searchBoundary, int limit)
			throws Exception {
		String lang = Helper.isEmpty(languages) ? "en": languages;
		String reqParams = "?text=" + URLEncoder.encode(GeocodingUtils.sanitizeAddress(address), "UTF-8") + "&size=" + limit + "&lang=" + lang; 
		
		if (searchBoundary != null)
		{
			if (searchBoundary instanceof RectSearchBoundary)
			{
				RectSearchBoundary rsb = (RectSearchBoundary)searchBoundary;
				Envelope env = rsb.getRectangle();
				reqParams += "&boundary.rect.min_lat=" + env.getMinY() + "&boundary.rect.min_lon=" + env.getMinX() + "&boundary.rect.max_lat=" + env.getMaxY() + "&boundary.rect.max_lon=" + env.getMaxX();
			}
			else if (searchBoundary instanceof CircleSearchBoundary)
			{
				CircleSearchBoundary csb = (CircleSearchBoundary)searchBoundary;
				reqParams += "&boundary.circle.lat=" + csb.getLatitude() + "&boundary.circle.lon=" + csb.getLongitude() + "&boundary.circle.radius=" + csb.getRadius();
			}
		}
		
		String respContent = HTTPUtility.getResponse(geocodingURL + reqParams, 10000, userAgent, "UTF-8");
		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) {

			return getGeocodeResults(respContent, searchBoundary);
		}

		return null;
	}

	@Override
	public GeocodingResult[] reverseGeocode(double lon, double lat, int limit) throws Exception {
		String reqParams = "?point.lat=" + lat  + "&point.lon=" + lon + "&size=" + limit;
		String respContent = HTTPUtility.getResponse(reverseGeocodingURL + reqParams, 10000, userAgent, "UTF-8");

		if (!Helper.isEmpty(respContent) && !respContent.equals("[]")) 
			return getGeocodeResults(respContent, null);
		else
			return null;
	}

	private GeocodingResult[] getGeocodeResults(String respContent, SearchBoundary searchBoundary)
	{
		JSONObject features = new JSONObject(respContent);
		JSONArray arr = (JSONArray)features.get("features");

		GeocodingResult[] results = new GeocodingResult[arr.length()];

		for (int j = 0; j < arr.length(); j++) 
		{
			JSONObject feature = arr.getJSONObject(j);
			JSONObject geom = feature.getJSONObject("geometry");
			JSONArray coords = geom.getJSONArray("coordinates");
			double lon = Double.parseDouble(coords.get(0).toString());
			double lat = Double.parseDouble(coords.get(1).toString());
			
			if (searchBoundary != null && !searchBoundary.contains(lon, lat))
				continue;

			JSONObject props = feature.getJSONObject("properties");
			
			String country = props.optString("country"); // places that issue passports, nations, nation-states
			String state = props.optString("region");   //states and provinces
			String county = props.optString("county"); // official governmental area; usually bigger than a locality, almost always smaller than a region
			String municipality = props.optString("localadmin");  // localadmin 	local administrative boundaries
			String street = props.optString("street");
			String locality = props.optString("locality");
			
			if (!Helper.isEmpty(state) && Helper.isEmpty(county) && props.has("macroregion")) // a related group of regions. Mostly in Europe
			{
				county = state;
				state = props.optString("macroregion");
			}
			
			if (locality != null && locality.equals(county))
			{
				county = props.optString("macrocounty");
			}
	
			float accuracy = (float)props.getDouble("confidence");
			
			if (state != null || county != null || locality != null || street != null)
            {
				GeocodingResult gr = new GeocodingResult();
				gr.locality = locality;
				gr.municipality = municipality;
				gr.country = country;
				gr.countryCode = props.optString("country_a");
				gr.state = state;
				//gr.stateDistrict = state_district;
				gr.county = county;
				gr.postalCode = props.optString("postalcode");
				gr.street = street;
				gr.neighbourhood = props.optString("neighbourhood");  // social communities, neighbourhoods
				gr.borough = props.optString("borough"); // a local administrative boundary, currently only used for New York City. also in Berlin
				gr.name = props.optString("name");
				gr.houseNumber = props.optString("housenumber");
				gr.longitude = lon;
				gr.latitude = lat;
				gr.accuracy = accuracy;
				
				results[j] = gr;
			}
		}

		Arrays.sort(results, new GeocodingResultComparator());
		
		return results;
	}
}
