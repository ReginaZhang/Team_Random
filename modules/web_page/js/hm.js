var serverAdd = "http://45.56.85.191";
var defaultMethod = "POST";
var userId = null;
var userName = null;
var userWeight = null;
var activeDietId = null;

$('document').ready(isLoginedIn());

function isLoginedIn() {

    $.getJSON("http://api.ipify.org?format=json", "", function(data) {

            var userIp = data.ip;

            query(":8000/user/check", defaultMethod, {userIp: userIp}, function(userDetail) {

                userId = userDetail.userId;
                userName = userDetail.userName;
                userWeight = userDetail.weight;

                init();

            });

    });

}

function init() {
    //showDiet();

    if(userId) {

        var trial = document.getElementById("trial");
        trial.parentNode.removeChild(trial);

        tabSwitcher();

        var user = {
            userId: userId
        };
        query(":8000/diet", defaultMethod,  user, function(userDiet) {
            generateDashboard(userDiet);
            generateDiet(userDiet);
        });

        generateExercise();
        generateStats();

    } else {

        $("#trialBody").html("Not logged in!");
        var reg = document.getElementById("registered");
        reg.parentNode.removeChild(reg);
        console.log("Not reg")


    }

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


function generateDashboard(userDiet) {
    var nameBar = document.getElementById("nameBar");

    $("#nameBar").empty();

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


    var diet = "";

    if (!userDiet[1].status) {

        $("#empty-warning").empty();

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
    }

    document.getElementById("todaysDietItems").innerHTML = diet;

}

function generateDiet(userDiet) {

    $(".item-Mon, .item-Tue, .item-Wed, .item-Thu, .item-Fri, .item-Sat, .item-Sun, .item-All, " +
    "#diet_nutrition, #diet_list, #empty-warning").empty();

    var dietNutri = {};

    [].forEach.call(Object.keys(userDiet[0]), function(key) {

        var option = document.createElement("option");

        option.value = userDiet[0][key];

        if (key.indexOf("otherDiet") != -1) {
            option.className = "diet-option";
            option.innerHTML = option.value;
        } else {
            option.className = "active diet-option";
            option.innerHTML = option.value + " (Active)";
            activeDietId = option.value;
        }

        $("#diet_list").append(option);

    });

    if (!userDiet[1].status) {
        for (var i = 1; i < userDiet.length; i++) {

            var thisFood = userDiet[i];
            var selector = "";
            var foodName = "";
            var weekday = "";
            var mealType = "";

            [].forEach.call(Object.keys(thisFood), function(key) {

                if (key == "foodName") {
                    foodName = thisFood[key];
                } else if (key == "weekday") {
                    weekday = thisFood[key];
                } else if (key == "mealType") {
                    mealType = thisFood[key];
                } else if (key == "foodId") {

                } else {
                    dietNutri[key] = (dietNutri[key] === undefined) ? Number(thisFood[key]) : dietNutri[key] + Number(thisFood[key]);
                }

            });

            var mealId = (mealType == "B" ? "#breakfast" : (mealType == "L" ? "#lunch" :
                (mealType === "D" ? "#dinner" : "#other")));

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

        [].forEach.call(Object.keys(dietNutri), function(key) {

            var oneNutri = document.createElement("p");
            oneNutri.id = key;

            var nutriName = "";
            var ch;
            var bracketEnding = false;


            for(var i=0; i<key.length; i++) {
                ch = key.charAt(i);

                if (isNaN(ch*1)) {

                    if (ch == "_") {
                        nutriName += "(";
                        bracketEnding = true;
                    } else if (ch == ch.toLowerCase()) {
                        nutriName += ch;
                    } else if (ch == ch.toUpperCase()) {
                        if(key.charAt(i-1) != "_") {
                            nutriName += " ";
                        }
                        nutriName += ch;
                    }

                } else {

                    nutriName += ch;

                }

            };

            if(bracketEnding) {
                nutriName += ")";
            }

            oneNutri.innerHTML = nutriName.trim() + ": " + dietNutri[key] + "<br>";

            $("#diet_nutrition").append(oneNutri);

        });
    } else {
        $("#empty-warning").html("Current diet is empty!");
    }

}

function generateExercise() {

}

function generateStats() {

}

function changeDiet(dietId) {

    query(":8000/diet", defaultMethod, {userId: userId, dietId: dietId}, function(userDiet) {
        generateDashboard(userDiet);
        generateDiet(userDiet);
    });

}

function addToDiet(foodId) {

    var user = {
        userId: userId,
        dietId: activeDietId,
        modiType: "add"
    };
    query(":8000/diet/modify", defaultMethod, user, function(userDiet) {
        generateDashboard(userDiet);
        generateDiet(userDiet);
    });

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

                result += '<input type="submit" class="addButtons" value="add to diet" ' +
                'onclick="addToDiet(' + response[i]["foodId"] + ')>';

                if (i != response.length - 1) {
                    result += "<br>";
                }
            }

            document.getElementById(form.id + "Result").innerHTML = result;
        }

    });

}

