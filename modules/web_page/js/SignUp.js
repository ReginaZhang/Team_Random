/**
 * Created by liubingfeng on 29/04/15.
 */

//initialize google map
function initialize(my_center_obj) {
    //position
    var myCenter=my_center_obj;
    console.log("in initialize "+my_center_obj);
    if (myCenter==null)
    {
        myCenter=new google.maps.LatLng(-37.799541, 144.960140);
    }
    var searchBox = new google.maps.places.SearchBox(document.getElementById("autocomplete"));
    var mapProp = {
        center:myCenter,
        zoom:11,
        mapTypeId:google.maps.MapTypeId.ROADMAP,
        panControl:true,
        zoomControl:true,
        mapTypeControl:true,
        scaleControl:true,
        streetViewControl:true,
        overviewMapControl:true,
        rotateControl:true
    };
    var map=new google.maps.Map(document.getElementById("googleMap"),mapProp);
    var marker=new google.maps.Marker({
        position:myCenter,
        animation:google.maps.Animation.BOUNCE
    });
    marker.setMap(map);
    google.maps.event.addListener(searchBox, 'places_changed',function(){codeAddress();});
}

google.maps.event.addDomListener(window, 'load', function(){initialize(null);});

function codeAddress()
{
    var geocoder = new google.maps.Geocoder();
    var address = document.getElementById('autocomplete').value;
    geocoder.geocode( { 'address': address}, function(results, status)
    {
        if (status == google.maps.GeocoderStatus.OK) {
            console.log("in status ok : " + results[0].geometry.location);
            initialize(results[0].geometry.location);
        } else {
            console.log('Geocode was not successful for the following reason: ' + status);
        }
    });
    console.log("address: "+address);
    return null;
}