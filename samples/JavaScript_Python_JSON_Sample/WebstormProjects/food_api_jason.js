/**
 * Created by liubingfeng on 12/03/15.
 */
console.log("hello");
function get_json() {
    var request = new XMLHttpRequest();
    //alert("hello");
    request.onreadystatechange = function () {
        //alert(request.status+"  ready:" + request.readyState +"body: "+request.responseBody);
        if (request.readyState == 4 && request.status == 200) {
            var myJSON = JSON.parse(request.responseText);
            console.log(JSON.stringify(myJSON.list.item, null, 2));
            print_json(myJSON);
        }
    };
    request.open('GET', "http://localhost:1234/get_list", true);
    request.send();
}
function print_json(myJSON) {
    var mylist = myJSON.list.item;
    //alert("hello" + JSON.stringify(mylist[0].id,null,2));
    //alert("hello too" +mylist[0].id);
    var divHTML = "";
    for (i = 0; i < mylist.length; i++) {
        divHTML += "<hr /><div>" + "<p> food id: " + mylist[i].id + "</p>" + "<p> food name:" + mylist[i].name + "</p>" + "<p> food offset: " + mylist[i].offset + "</p>" + "</div>";
    }
    document.body.innerHTML = divHTML;
}
//# sourceMappingURL=food_api_jason.js.map