/**
 * Created by liubingfeng on 13/03/15.
 */
/*function used to determine login window up or not*/



function insert_navigation_bar()
{
    var obj=document.getElementById("navigation_bar");
    obj.innerHTML='<nav class="navbar navbar-custom">\
        <div class="container-fluid">\
        <div class="navbar-header">\
        <a class="navbar-brand" href="index.html">Health Overflow</a>\
        </div>\
        <div>\
        <ul class="nav navbar-nav">\
        <li ><a class="nav_element home" href="index.html">Home</a></li>\
        <li ><a class="nav_element forum" href="http://45.56.85.191:80/index">Forum</a></li>\
        <li ><a class="nav_element health_manager" href="HealthManagerDemo.html">Health Manager</a></li>\
        <li ><a class="nav_element SignUp" href="SignUp.html">SignUP</a></li>\
        <li ><a class="nav_element Login" onclick="loginWindow();" href="#">Login</a></li>\
        <li ><a class="nav_element about" href="about.html">About</a></li>\
        </ul>\
        </div>\
        </div>\
        </nav>\
        ';
    console.log("in function");
}

function loginWindow()
{
    one_obj=document.getElementById("login_div");
    if(one_obj==null)
    {
        loginPOPup(one_obj);
    }
    else
    {
        loginPOPdown(one_obj);
    }
}

function loginPOPup(one_obj)
{
    one_obj=document.createElement('div');
    one_obj.setAttribute("id","login_div");
    one_obj.innerHTML="<form id='login_form' >\
            <div id='login_error_div'></div>\
            <div class='form-group'>\
    <label class='login_label'>User Name</label>\
    <input type='text' class='form-control' id='input_username' placeholder='User Name' autofocus>\
    </div>\
    <br/>\
    <div class='form-group'>\
    <label class='login_label'>Password</label>\
    <input type='password' class='form-control' id='input_password' placeholder='Password'>\
    </div>\
    <br/>\
    <button type='button' class='btn btn-default' onclick='login_validation();'>Login</button>\
    <button type='button' onclick='sign_up();' class='btn btn-default'>sign_up</button>\
    <button type='button' onclick='loginWindow();' class='btn btn-default'>Close</button>\
    </form>\
    ";
    document.body.appendChild(one_obj);
    console.log("in pop up : " + one_obj.innerHTML);
}
function loginPOPdown(one_obj)
{
    one_obj.remove();
    console.log("in pop down");
}

function login_validation()
{

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "http://45.56.85.191:8000/user/login", true);
    var user_data = {};
    user_data["username"] =document.getElementById("input_username").value;
    user_data["password"] =document.getElementById("input_password").value;
    //user_data["email"] = "hello@gmail.com";
    if(!user_data_validation(user_data))
    {
        return false;
    }
    xhr.send(JSON.stringify(user_data));
    console.log(JSON.stringify(user_data));

    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status==200) {
            console.log(xhr.responseText);
        }
    }
}

function user_data_validation(user_data)
{
    if(user_data["username"].length==0)
    {
        document.getElementById("login_error_div").innerHTML="username shouldn't be empty";
        return false;
    }
    if(user_data["password"].length==0)
    {
        document.getElementById("login_error_div").innerHTML="password shouldn't be empty";
        return false;
    }
    return true;
}

insert_navigation_bar();