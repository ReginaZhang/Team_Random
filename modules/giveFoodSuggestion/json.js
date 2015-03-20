//Example from https://stackoverflow.com/questions/1255948/post-data-in-json-format-with-javascript
//Slightly modified

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
      }
    }

    xhr.onloadend = function () {
      // done
    };

};





function notRegistered() {
  document.getElementsByName("height")[0].style.display = "inline";
  document.getElementsByName("weight")[0].style.display = "inline";
  document.getElementsByName("memberId")[0].value = "";
  document.getElementById("submitButton").value="Get BMI!"
}

function registered() {
  document.getElementsByName("height")[0].style.display = "none";
  document.getElementsByName("weight")[0].style.display = "none";
  document.getElementsByName("memberId")[0].value = 1;
  document.getElementById("submitButton").value="Get Ray's BMI!"
}