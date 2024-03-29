//var user_data_g=null;
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
        zoom:15,
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



//

function changeDate(i){
    //console.log("hehe");
    var e = document.getElementById('day');
    var s;
    while(e.length>0)
    {
        e.remove(e.length-1);
    }
    var j=-1;
    //console.log("j : "+j);
    if(i=="na")
    {
        k=0;
    }
    else if(i==2)
    {
        k=28;
    }
    else if(i==4||i==6||i==9||i==11)
    {
        k=30;
    }
    else
    {
        k=31;
    }
    //console.log("hehe");
    //console.log("j : "+j);
    while(j++<k){
        s=document.createElement('option');
        e=document.getElementById('day');
        if(j==0){
            //console.log("hahah");
            s.text="Day";
            s.value="na";
            try{
                e.add(s,null);
            }
            catch(ex){
                e.add(s);
            }
        }
        else{
            //console.log("hahah2");
            s.text=j;
            s.value=j;
            try{
                e.add(s,null);
            }
            catch(ex){
                e.add(s);
            }
        }
    }
    y = 1998;
    while (y-->1908){
        //console.log("hahah3");
        s = document.createElement('option');
        e = document.getElementById('year');
        s.text=y;
        s.value=y;
        try{
            e.add(s,null);
        }
        catch(ex){
            e.add(s);
        }
    }
}

function unhide(divID) {
    var item = document.getElementById(divID);
    if (item) {
        item.className=(item.className=='hidden')?'unhidden':'hidden';
        //console.log("in function , equality : "+divID+item.className);
    }

}

function makeNULL(user_data)
{
    if(user_data["height"]=="")
    {
        user_data["height"]=null;
    }
    if(user_data["weight"]=="")
    {
        user_data["weight"]=null;
    }
    if(user_data["address"]=="")
    {
        user_data["address"]=null;
    }
    return user_data;
}

function sign_up()
{
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "http://45.56.85.191:8000/user/register", true);
    var user_data = {};
    user_data["username"]=document.getElementById("username").value;
    user_data["password"]=document.getElementById("password").value;
    user_data["confirm_password"]=document.getElementById("confirm_password").value;
    //user_data["address"]=document.getElementById("autocomplete").value;
    user_data["email"] = document.getElementById("email").value;
    user_data["address"] = document.getElementById("autocomplete").value;
    user_data["height"] = document.getElementById("height").value;
    user_data["weight"] = document.getElementById("weight").value;
    //makeNULL(user_data);
    //user_data["username"] =document.getElementById("input_username").value;
    //user_data["password"] =document.getElementById("input_password").value;
    if(!check_sign_up_user_data(user_data))
    {
        return;
    }
    xhr.send(JSON.stringify(user_data));
    //console.log(JSON.stringify(user_data));
    console.log(user_data);
    //user_data_g=user_data;
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status==200) {
            console.log(xhr.responseText);
            if(JSON.parse(xhr.responseText).register=="successful")
            {
                //change this to specific url from our vps
                login_validation(user_data["username"],user_data["password"],function(){
                    window.location.replace("http://45.56.85.191/static/index.html");
                });
            }
            else
            {
                var error_div= document.getElementById("sign_up_error_div");
                error_div.innerHTML=xhr.responseText;
            }
        }
    }


}

function check_sign_up_user_data(user_data)
{
    console.log("test sign up validation "+(JSON.stringify(user_data)));
    var user_name_regex=/[a-zA-Z]*/;
    var email_regex=/[a-zA-Z0-9]*@[a-zA-Z0-9]*[\.a-zA-Z0-9]*\.[A-Za-z0-9]*[^.]$/;
    var error_div= document.getElementById("sign_up_error_div");
    error_div.innerHTML="";
    if(user_data["password"]==""||user_data["confirm_password]"]=="")
    {
        error_div.innerHTML="Your password cant be empty X.X";
        return false;
    }
    if(user_data["password"]!=user_data["confirm_password"])
    {
        error_div.innerHTML="Your re-entered password does not match X.X";
        return false;
    }

    if((user_data["email"].match(email_regex)==null)||(user_data["email"]!=user_data["email"].match(email_regex)[0]))
    {
        error_div.innerHTML="Your email is invalid X.X";
        return false;
    }
    return true;

}

