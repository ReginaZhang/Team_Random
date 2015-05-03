/**
 * Created by ra on 15-5-3.
 */

$('document').ready(init());

var serverAdd = "http://45.56.85.191:8000";
var method = "POST";

function init() {
    //showDiet();

    if(isRegistered()) {

        var trial = document.getElementById("trial");
        trial.parentNode.removeChild(trial);


        var reg = document.getElementById("registered");

        document.getElementById("tabNav").innerHTML =
            "<ul id='tabNavMenu'>" +
            "<li id='dashbdTab' class='selected' onclick='switchingTabs(0)'>Dashboard</li>" +
            "<li id='fdTab' onclick='switchingTabs(1)'>Food & Diet</li>" +
            "<li id='exerciseTab' onclick='switchingTabs(2)'>Health Test</li>" +
            "<li id='statsTab' onclick='switchingTabs(3)'>Stats</li>" +
            "</ul>";

        generateDashbd();

    } else {

        var reg = document.getElementById("registered");
        reg.parentNode.removeChild(reg);

    }


}

function isRegistered() {
    return true;
}

function switchingTabs(index) {
    var tnm = document.getElementById("tabNavMenu").childNodes;
    if (tnm[index].className != "selected") {

        for (var i = 0; i < tnm.length; i++) {
            if (tnm[i].className == "selected") {
                tnm[i].className = null;
            }
        }

        tnm[index].className = "selected";
        var body = document.getElementById("regBody");

        /*
        if (index == 0) {
            generateDashbd(body);
        } else if (index == 1) {
            generateFd(body);
        } else if (index == 2) {
            generateExercise(body);
        } else if (index == 3) {
            generateStats(body);
        }
        */


    }

}

function query(directory, data) {

    var xhr = new XMLHttpRequest();
    xhr.open(method, serverAdd + directory, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    // send the collected data as JSON
    xhr.send(JSON.stringify(data));

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {
            return JSON.parse(xhr.responseText);
        }
    }

}

function generateDashbd(body) {

    var nameBar = document.createElement("table");

    var userName = "ra";
    var userWeight = 62;
    var data = {
        hasRegistered: true,
        userId: 1
    };
    var userBmi = query("/bmi", data);

    var name = document.createElement("p").innerHTML = "Hi! " + userName;
    var weight = document.createElement("p").innerHTML = "Weight: " + userWeight + "kg";
    var bmi = document.createElement("p").innerHTML = "BMI: " + userBmi["bmi"] + ", " + userBmi["status"];

    body.appendChild(name, weight, bmi);

}

function submitForm(event, form) {

    event.preventDefault();

    // collect the form data while iterating over the inputs
    var data = {};
    for (var i = 0, ii = form.length; i < ii; ++i) {
        var input = form[i];
        if (input.name) {
            if (input.type != "radio" || input.checked == true) {
                data[input.name] = input.value;
            }
        }
    }

    // construct an HTTP request
    var xhr = new XMLHttpRequest();
    xhr.open(form.method, form.action, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    // send the collected data as JSON
    xhr.send(JSON.stringify(data));
    //document.getElementById("response").innerHTML = JSON.stringify(data)

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {
            document.getElementById(form.id + "_response").innerHTML = xhr.responseText;

            if (form.id == "modify_diet") {
                showDiet();
            }

        }
    }

    xhr.onloadend = function () {
        // done
    };

};


function registered(truthValue) {

    if (truthValue == true) {
        document.getElementsByName("height")[0].style.display = "none";
        document.getElementsByName("weight")[0].style.display = "none";
        document.getElementsByName("userId")[0].value = 1;
        document.getElementById("submitButton").style.display = "none";
    } else if (truthValue == false){
        document.getElementsByName("height")[0].style.display = "inline";
        document.getElementsByName("weight")[0].style.display = "inline";
        document.getElementsByName("userId")[0].value = "";
        document.getElementById("submitButton").value="Get BMI!";
    }


}

function addOrDelete(truthValue) {
    if (truthValue == true) {
        document.getElementById("submitButton2").value="Add to Ray's diet";
    } else {
        document.getElementById("submitButton2").value="Delete from Ray's diet";
    }
}

function showDiet(){

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "http://45.56.85.191:8000/diet", true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    var data = {};
    data["dietId"] = "1";
    xhr.send(JSON.stringify(data))

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {
            document.getElementById("diet").innerHTML = xhr.responseText;
        }
    }


}
