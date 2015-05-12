var serverAdd = "http://45.56.85.191";
var defaultMethod = "POST";
var userId = null;
var userName = null;
var userWeight = null;
var activeDietId = null;

$('document').ready(isLoginedIn());

/*
    Check whether current user has logged in
 */
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

/*
    Initialization funciton
 */
function init() {

    //based on whether use has logged in, display two different web page
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

/*
    Tab-switching listener
 */
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

/*
    Generating the first tab
 */
function generateDashboard(userDiet) {
    var nameBar = document.getElementById("nameBar");

    $("#nameBar").empty();
    $("#todaysDietItems").empty();

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
    var dietsIndex;
    var todayDiet = document.getElementById("todaysDietItems");
    var breakfast = document.createElement("p");
    breakfast.innerHTML = "Breakfast<br>";
    var lunch = document.createElement("p");
    lunch.innerHTML = "Lunch<br>";
    var dinner = document.createElement("p");
    dinner.innerHTML = "Dinner<br>";
    var other = document.createElement("p");
    other.innerHTML = "Other<br>";


    for (dietsIndex = 0; (userDiet[dietsIndex].dietId); dietsIndex++) {
    }

    if (!userDiet[dietsIndex].status) {

        var today = new Date();
        var whichWeekDay = today.getDay();
        var weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
        var found = false;

        for (var i = dietsIndex; i < userDiet.length; i++) {

            if (userDiet[i].weekday == weekDays[whichWeekDay]) {

                found = true;
                if(userDiet[i].mealType == "B") {
                    breakfast.innerHTML += userDiet[i].foodName + "<br>";
                } else if(userDiet[i].mealType == "L") {
                    lunch.innerHTML += userDiet[i].foodName + "<br>";
                } else if(userDiet[i].mealType == "D") {
                    dinner.innerHTML += userDiet[i].foodName + "<br>";
                } else {
                    other.innerHTML += userDiet[i].foodName + "<br>";
                }

                console.log(found);
            }

        }

        if(!found) {
            todayDiet.innerHTML = "You have nothing in the diet for today!";
        } else {
            todayDiet.appendChild(breakfast);
            todayDiet.appendChild(lunch);
            todayDiet.appendChild(dinner);
            todayDiet.appendChild(other);
        }

    } else {
        todayDiet.innerHTML = "You have nothing in the diet for today!";
    }

}

/*
    Generating the second tab
 */
function generateDiet(userDiet) {

    $(".item-Mon, .item-Tue, .item-Wed, .item-Thu, .item-Fri, .item-Sat, .item-Sun, .item-All, " +
    "#diet_nutrition, #diet_list, #empty-warning").empty();

    var dietNutri = {};

    var dietsIndex;
    var selectElement = $("#diet_list");
    var activeIndex = 0;

    for (dietsIndex = 0; (userDiet[dietsIndex].dietId); dietsIndex++) {

        var option = document.createElement("option");

        option.className = userDiet[dietsIndex].activeType + " diet-option";
        option.innerHTML = userDiet[dietsIndex].dietName;
        option.value = userDiet[dietsIndex].dietId;


        if (userDiet[dietsIndex].activeType == "active") {
            activeDietId = userDiet[dietsIndex].dietId;
            activeIndex = dietsIndex;
            selectElement.append(option);
        } else if (userDiet[dietsIndex].activeType == "current") {
            option.innerHTML = option.innerHTML + " (Current Diet)";
            selectElement.prepend(option);
            activeIndex += 1;
        } else {
            selectElement.append(option);
        }

    }

    selectElement.prop("selectedIndex", activeIndex.toString());

    if (!userDiet[dietsIndex].status) {
        for (var i = dietsIndex; i < userDiet.length; i++) {

            $("#empty-warning").empty();

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
                } else if (key == "foodId" || key.indexOf("")) {

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

            }

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

/*
    Retrieve foods in the given diet
 */
function getDiet(dietId) {

    query(":8000/diet", defaultMethod, {userId: userId, dietId: dietId}, function(userDiet) {
        generateDashboard(userDiet);
        generateDiet(userDiet);
    });

}

/*
    Mark the diet displayed on the page as the current effective diet
 */
function startDiet() {

    var data = {
        dietId: activeDietId,
        modiType: "start",
        foodId: null,
        weekday: null,
        mealType: null
    };

    query(":8000/diet/modify", defaultMethod, data, function(status) {
        if(status.update == "successful") {
            getDiet(activeDietId);
        }

    });

}

function createDiet() {

}

/*
    Add nominated food to active diet
 */
function addToDiet(foodId, weekday, mealType) {

    var user = {
        userId: userId,
        dietId: activeDietId,
        modiType: "add",
        foodId: foodId,
        weekday: weekday,
        mealType: mealType

    };
    query(":8000/diet/modify", defaultMethod, user, function() {
        getDiet(activeDietId);
    });

}

/*
    General query from backend function
 */
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

/*
    General form submitting function
 */
function submitForm(event, form) {

    event.preventDefault();

    //determine which url based on the id of the form submitted
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

    //handle respond
    query(":8000"+dir, defaultMethod, data, function(response) {

        //based on which form
        if (form.id == "foodNutrition") {
            var result = "";

            for (var i = 0; i < response.length; i++) {
                result += response[i]["foodName"];

                result += '<button type="button" class="add-to-diet-button" ' +
                'onclick="addToDiet(' + Number(response[i]["foodId"]) + ',' +
                '$(\'#addToDietWeekday' + response[i]["foodId"] + '\').val(),$(\'#addToDietMeal' + response[i]["foodId"] + '\').val()' +
                ');">Add to diet</button><select id="addToDietWeekday' + response[i]["foodId"] + '">' +
                '<option value="Mon">Monday</option>' +
                '<option value="Tue">Tuesday</option>' +
                '<option value="Wed">Wednesday</option>' +
                '<option value="Thu">Thursday</option>' +
                '<option value="Fri">Friday</option>' +
                '<option value="Sat">Saturday</option>' +
                '<option value="Sun">Sunday</option>' +
                '<option value="NA">Not Specified</option>' +
                '</select>' +
                '<select id="addToDietMeal' + response[i]["foodId"] + '">' +
                '<option value="B">Breakfast</option>' +
                '<option value="L">Lunch</option>' +
                '<option value="D">Dinner</option>' +
                '<option value="O">Backup/Other</option>' +
                '</select>';

                if (i != response.length - 1) {
                    result += "<br>";
                }

                console.log(result);

            }

            document.getElementById(form.id + "Result").innerHTML = result;
        }

    });

}

