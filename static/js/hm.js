var serverAdd = "http://45.56.85.191";
var defaultMethod = "POST";
var userId =null;
var userName = null;
var userWeight = null;
var activeDietId = null;

$('document').ready(isLoginedIn());

/*
    Check whether current user has logged in
 */
function isLoginedIn() {

    $.getJSON("http://api.ipify.org?format=json&antiCache="+Math.random(), "", function(data) {

            var userIp = data.ip;

            query(":8000/user/check", defaultMethod, {userIp: userIp}, function(userDetail) {

                userId = 7;
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
    console.log("user id "+userId);
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

            $("#diet-plan-title").html("Weekly Diet Plan  -  " + userDiet[dietsIndex].dietName);
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
        if(userDiet[dietsIndex].status == "noDiet") {
            $("#empty-warning").html("You currently have no diet!");
        } else {
            $("#empty-warning").html("Current diet is empty!");
        }

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
        ndbno: null,
        weekday: null,
        mealType: null,
        userId: userId
    };

    query(":8000/diet/modify", defaultMethod, data, function(status) {
        if(status.update == "successful") {
            getDiet(activeDietId);
        }

    });

}

function createDiet() {

    var user = {
        userId: userId
    };

    var data = {
        userId: userId,
        dietName:"Testing",
        dietType: 'G'
    };

    query(":8000/diet/create", defaultMethod, data, function(status) {
        if(status.dietCreate == "successful") {
            activeDietId = status.dietId;

            query(":8000/diet", defaultMethod,  user, function(userDiet) {
                generateDashboard(userDiet);
                generateDiet(userDiet);
            });
        } else {
            //TODO POPUP ERROR
        }
    })

}

function deleteDiet() {

    $.getJSON("http://api.ipify.org?format=json&antiCache="+Math.random(), "", function(data) {
        query(":8000/diet/delete", defaultMethod, {dietId: activeDietId, userId: userId, userIp: data.ip}, function (status) {

            if (status.dietDelete == "successful") {

                query(":8000/diet", defaultMethod, {userId: userId}, function (userDiet) {
                    generateDashboard(userDiet);
                    generateDiet(userDiet);
                });

            } else {
                //TODO POPUP ERROR
            }
        });
    });
}

/*
    Add nominated food to active diet
 */
function addToDiet(ndbno, weekday, mealType) {

    var user = {
        userId: userId,
        dietId: activeDietId,
        modiType: "add",
        ndbno: ndbno,
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
var page=1;
var itemList = [];
var dbOffset = 0;
var apiOffset = 0;
var more = true;
function getFood() {

    var searchTerm = document.getElementById("search_box").value;
    var i=0;

    query("/food/search_food?dboffset="+dbOffset+"&apioffset="+apiOffset+"&term="+searchTerm, "GET", {}, function(responseJson) {

        dbOffset = responseJson.dboffset;
        apiOffset = responseJson.apioffset;
        if (responseJson.items.length == 0){
            more = false;
        } else {
            for(i = 0; i<responseJson.items.length; i++){
                itemList.push(responseJson.items[i]);
            }
        }
        console.log(dbOffset);
        console.log(apiOffset);
        console.log(itemList.length);

        displayFoods();
    });

}

function displayFoods(){
    var food;
    var result = "";
    if (more) {
        for (var i = (page - 1)*10; i < page*10; i++){
            if (i >= itemList.length){
                break;
            }
            food = itemList[i];
            result += '<div class="one_food_div"><span class="food_name" onclick="showFoodNutrientsTable(this)">'+food.foodname +'</span>'+'<button type="button" class="add_to_diet_button" onclick="addToDiet(' + Number(food.ndbno) + ',' +
                    '$(\'#addToDietWeekday' + food.ndbno + '\').val(),$(\'#addToDietMeal' + food.ndbno + '\').val()' +
                    ');">Add to diet</button><select id="addToDietWeekday' + food.ndbno + '">' +
                    '<option value="Mon">Monday</option>' +
                    '<option value="Tue">Tuesday</option>' +
                    '<option value="Wed">Wednesday</option>' +
                    '<option value="Thu">Thursday</option>' +
                    '<option value="Fri">Friday</option>' +
                    '<option value="Sat">Saturday</option>' +
                    '<option value="Sun">Sunday</option>' +
                    '<option value="NA">Not Specified</option>' +
                    '</select>' +
                    '<select id="addToDietMeal' + food.ndbno + '">' +
                    '<option value="B">Breakfast</option>' +
                    '<option value="L">Lunch</option>' +
                    '<option value="D">Dinner</option>' +
                    '<option value="O">Backup/Other</option>' +
                    '</select><br></div>'+
                    '<div class="individual_food_nutrients_div" id="'+food.foodname+'"></div>';
        }
        result += '<br>';
        if (page > 1){
            result += '<button type="button" id="previous_page_button" onclick="page-=1;displayFoods()">previous</button>';
        }
        if (page <= (itemList.length / 10 + 1)){
            console.log(page);
            console.log(itemList.length /10);
            result += '<button type="button" id="next_page_button" onclick="page+=1;displayFoods()">next</button>';
        } else {
            result += '<button type="button" id="view_more_button" onclick="page+=1;getFood()">I want more &gt;.&lt;</button>';
        }
    } else {
        result += 'Sorry, we couldn\'t find more for you T^T <br><br><button type="button" id="previous_page_button" onclick="page-=1;displayFoods()">previous</button>';
    }
    document.getElementById('foodNutritionResult').innerHTML = result;

}


function findFoodNdbno(food_name)
{
    var i=0;
    var ndbno=0;
    for(i=0;i<itemList.length;i++)
    {
        console.log("foodname "+itemList[i].foodname +" div food name "+food_name);
        if(itemList[i].foodname==food_name)
        {
            ndbno=itemList[i].ndbno;
            return ndbno;
        }
    }
    return ndbno;
}

function showFoodNutrientsTable(obj)
{
    var food_name = obj.innerHTML;
    var ndbno=0;
    var html_format='';
    var food_nutrient_div=document.getElementById(food_name);
    if(food_nutrient_div.innerHTML!="")
    {
        food_nutrient_div.innerHTML="";
        return;
    }
    ndbno=findFoodNdbno(food_name);
    getIndividualFoodNutrition(ndbno,food_nutrient_div);
    console.log(" hahhaha html "+html_format+food_nutrient_div);
    //food_nutrient_div.innerHTML=html_format;
}

function getIndividualFoodNutrition(ndbno,food_nutrient_div)
{
    console.log("ndbno "+ndbno);
    var request = new XMLHttpRequest();
    request.open("GET", "http://45.56.85.191/food/get_food_report?ndbno="+ndbno);
    request.send();
    request.onreadystatechange = function() {
        if (request.readyState == 4 && request.status==200)
        {

            console.log("12312321"+JSON.parse(request.responseText).nutrients);
            console.log("get json."+food_nutrient_div);
            formatingNutrientsDiv(JSON.parse(request.responseText),food_nutrient_div);

        }
    };
}

function formatingNutrientsDiv(food_nutrients_json,food_nutrient_div)
{
    //var obj=document.getElementById(div_name);
    console.log("in function"+food_nutrient_div);
    var html='<p class="food_name">'+food_nutrient_div.id+'</p>\
                    <hr/>\
                    <table class="food_nutrients_table" border="1">\
                    <tr>\
                    <th>Nutrient Name</th>\
                    <th>Value</th>\
                    <th>Unit</th>\
                    </tr>';
    var key;
    //nutrients info
    for(key in food_nutrients_json.nutrients)
    {
        html+='<tr>'
        html+='<td>'+key+'</td>'
        //console.log("check "+food_nutrients_json.nutrients[key].value);
        html+='<td>'+food_nutrients_json.nutrients[key].value+'</td>';
        html+='<td>'+food_nutrients_json.nutrients[key].unit+'</td>';
        html+='</td></tr>'
    }
    html+='</tr>'
    html+='</table>';
    console.log("hahha"+html);
    food_nutrient_div.innerHTML=html;
    return html;
}
