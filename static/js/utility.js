/**
 * Created by liubingfeng on 13/05/15.
 */
function getUserIP()
{
    var DetailsIP=null;
    var xhr_ip = new XMLHttpRequest();
    xhr_ip.open("GET", "http://ipinfo.io/json");
    //xhr.setRequestHeader('Content-Type', 'application/json'); seems not needed!
    // send the collected data as JSON
    xhr_ip.send();
    xhr_ip.onreadystatechange = function() {
        console.log("in funx");
        if (xhr_ip.readyState == 4 &&xhr_ip.status==200) {
            console.log(" "+xhr_ip.responseText);
            DetailsIP=JSON.parse(xhr_ip.responseText);
        }

    };
    return
}