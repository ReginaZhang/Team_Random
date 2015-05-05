
var serverAdd = "http://45.56.85.191";
var defaultMethod = "POST";
var userId = null;
var userName = null;
var userWeight = null;

$('document').ready(init());

function init() {
    //showDiet();

    if(isLoginedIn()) {

        var trial = document.getElementById("trial");
        trial.parentNode.removeChild(trial);

        tabSwitcher();
        generateDashboard();
        generateDiet();
        generateExercise();
        generateStats();

    } else {

        var reg = document.getElementById("registered");
        reg.parentNode.removeChild(reg);

        document.getElementById("nav").innerHTML =
            "<ul id='menu'>" +
            "<li><a href='Food.html'>Food Nutrition</a></li>" +
            "<li><a href='Test.html'>Health Test</a></li>" +
            "<li><a href='Plan.html'>Health Plan</a></li>" +
            "</ul>";

    }

}

function isLoginedIn() {

    /*
    var header = navigator.userAgent;

    var xhr = new XMLHttpRequest();
    xhr.open("GET", "http://api.hostip.info/get_json.php");
    //xhr.setRequestHeader('Content-Type', 'application/json'); seems not needed!
    // send the collected data as JSON
    xhr.send();

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {

            query("/check_loggedin?ip=" + JSON.parse(xhr.responseText).ip + "&header=" + header, "GET", {}, function(id){

                if (id.id != "null") {
                    userId = id.id;
                    query(":8000/user/check/", defaultMethod, {userId: userId}, function(userDetail) {

                        userName = userDetail.userName;
                        userWeight = userDetail.userWeight;

                    });

                }

            });
        }
    };
    */

    userId = 1;
    userName = "ra";
    userWeight = 62;

    return userId;


}

function tabSwitcher() {
    $('.tabs .tab-links a').on('click', function(e)  {
        var currentAttrValue = $(this).attr('href');

        // Show/Hide Tabs
        $('.tabs ' + currentAttrValue).show().siblings().hide();
        console.log("in function");
        // Change/remove current tab to active
        $(this).parent('li').addClass('active').siblings().removeClass('active');


        e.preventDefault();


    });

}


function generateDashboard() {
    var nameBar = document.getElementById("nameBar");

    var data = {
        hasRegistered: true,
        userId: userId
    };

    query(":8000/bmi", defaultMethod, data, function(userBmi) {
        var name = document.createElement("p");
        name.innerHTML = "Hi! " + userName;
        name.className = "nameBar";

        var weight = document.createElement("p");
        weight.innerHTML = "Weight: " + userWeight + "kg";
        weight.className = "nameBar";

        var bmi = document.createElement("p");
        bmi.innerHTML = "BMI: " + userBmi.bmi.slice(0,5) + ", " + userBmi.status;
        bmi.className = "nameBar";

        nameBar.appendChild(name);
        nameBar.appendChild(weight);
        nameBar.appendChild(bmi);
    });

    query(":8000/diet", defaultMethod,  data, function(userDiet) {

        var diet = "";

        if (!userDiet[0].status) {
            $("#emptyWarning").remove();

            for (var i = 1; i < userDiet.length; i++) {

                var today = new Date();
                var whichWeekDay = today.getDate();
                var weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
                var found = false;

                if (userDiet[i].weekday == weekDays[whichWeekDay]) {

                    found = true;
                    diet += userDiet[i].foodName;

                    if (i != userDiet.length - 1) {
                        diet += ",   ";
                    }

                }

            }

            if(!found) {
                diet = "You have nothing in the diet for today!";
            }

        } else {
            diet = "You have nothing in the diet for today!";
            $("#emptyWarning").html("Current diet is empty!");
        }

        document.getElementById("todaysDietItems").innerHTML = diet;

    });

}

function generateDiet() {

    var user = {
        userId: userId
    };

    query(":8000/diet", defaultMethod,  user, function(userDiet) {

        for (var i = 1; i < userDiet.length; i++) {

            var thisFood = userDiet[i];

            var foodName = thisFood.foodName;
            var weekday = thisFood.weekday;
            var meal = thisFood.mealType;


            var selector = "";

            var mealId = (meal == "B" ? "#breakfast" : (meal == "L" ? "#lunch" :
                (meal=== "D" ? "#dinner" : "#other")));

            if (mealId == "#other" ) {
                if (weekday != "NA") {
                    mealId += "_with_weekday";
                } else {
                    mealId += "_flexi";
                }
            }

            selector += mealId;
            selector += " .item-";

            if (weekday != "NA") {
                selector += weekday;
            } else {
                selector += "All";
            }

            $(selector).html((($(selector).html() !== undefined) ? $(selector).html() : "") + foodName + "<br>");


        }




    });






}

function generateExercise() {

}

function generateStats() {

}

function query(directory, method, data, callback) {

    var xhr = new XMLHttpRequest();
    xhr.open(method, serverAdd + directory);
    //xhr.setRequestHeader('Content-Type', 'application/json'); seems not needed!

    // send the collected data as JSON
    xhr.send(JSON.stringify(data));

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {
            callback(JSON.parse(xhr.responseText));
        }
    }

}

function submitForm(event, form) {

    event.preventDefault();

    var dir;
    if (form.id == "foodNutrition") {
        dir = "/food";
    }

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

    query(":8000"+dir, defaultMethod, data, function(response) {

        if (form.id == "foodNutrition") {
            var result = "";

            for (var i = 0; i < response.length; i++) {
                result += response[i]["foodName"];

                if (i != response.length - 1) {
                    result += "<br>";
                }
            }

            document.getElementById(form.id + "Result").innerHTML = result;
        }

    });

}


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
