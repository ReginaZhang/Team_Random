//Example from https://stackoverflow.com/questions/1255948/post-data-in-json-format-with-javascript
//Slightly modified

var form = document.getElementById("gimme_these_food");

form.onsubmit = function (e) {
  // stop the regular form submission
  e.preventDefault();

  // collect the form data while iterating over the inputs
  var data = {};
  for (var i = 0, ii = form.length; i < ii; ++i) {
    var input = form[i];
    if (input.name) {
      data[input.name] = input.value;
    }
  }

  // construct an HTTP request
  var xhr = new XMLHttpRequest();
  xhr.open(form.method, form.action, true);
  xhr.setRequestHeader('Content-Type', 'application/json');

  // send the collected data as JSON
  xhr.send(JSON.stringify(data));
  document.getElementById("response").innerHTML = JSON.stringify(data)

  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
        document.getElementById("response").innerHTML = xhr.responseText;
    }
}

  xhr.onloadend = function () {
    // done
  };
};