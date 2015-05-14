//Example from https://stackoverflow.com/questions/1255948/post-data-in-json-format-with-javascript
//Slightly modified

//$('document').ready(showDiet());

function submitForm(event, form) {

    event.preventDefault();

    document.getElementById("response").innerHTML = form.id;

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
          document.getElementById("response").innerHTML = xhr.responseText;
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
    document.getElementById("submitButton").value="Get Ray's BMI!";
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

/*function showDiet(){

  var xhr = new XMLHttpRequest();
  xhr.open("POST", "http://localhost:8888/diet", true);
  xhr.setRequestHeader('Content-Type', 'application/json');

  var data = {};
  data["dietId"] = "1";
  xhr.send(JSON.stringify(data))

  xhr.onreadystatechange = function() {
      if (xhr.readyState == 4) {
          document.getElementById("diet").innerHTML = xhr.responseText;
      }
  }

  
}*/


var page=1;
var itemList = [];
var dbOffset = 0;
var apiOffset = 0;
function getFood() {

    var searchTerm = document.getElementById("search_box").value;

    var request = new XMLHttpRequest();
    request.open("GET", "http://45.56.85.191:8888/search_food?dboffset="+dbOffset+"&apioffset="+apiOffset+"&term="+searchTerm,false);
    request.send();

    var responseJson = JSON.parse(request.responseText);

    dbOffset = responseJson.dboffset;
    apiOffset = responseJson.apioffset;
    itemList = responseJson.items;

    displayFoods();
}

function displayFoods(){
    var food;
    var result = "";
    for (var i = (page - 1)*10; i < page*10; i++){
        food = itemList[i];
        result += food.foodname +'<button type="button" class="add_to_diet_button" onclick="addToDiet(' + Number(food.ndbno) + ',' +
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
                '</select><br>';
    }
    result += '<br>';
    if (page > 1){
        result += '<button type="button" id="previous_page_button" onclick="page-=1;displayFoods()">previous</button>';
    }
    result += '<button type="button" id="next_page_button" onclick="page+=1;displayFoods()">next</button>';
    document.getElementById('searchResults').innerHTML = result;

}