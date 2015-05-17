//var userId=null;
/*function used to determine login window up or not*/

function insert_navigation_bar(user_id,user_details)
{
    var div_id='login_div';
    //console.log("uesr detials "+user_details.details.username);
    var obj=document.getElementById("navigation_bar");
    if ( (user_id==null)||(user_details==null))
    {
        obj.innerHTML='<nav class="navbar navbar-custom">\
        <div class="container-fluid">\
        <div class="navbar-header">\
        <a class="navbar-brand" href="index.html">Health Overflow</a>\
        </div>\
        <div>\
        <ul class="nav navbar-nav">\
        <li ><a class="nav_element home" href="index.html">Home</a></li>\
        <li ><a class="nav_element forum" href="http://45.56.85.191:80/index">Forum</a></li>\
        <li ><a class="nav_element health_manager" href="HealthManager.html">Health Manager</a></li>\
        <li ><a class="nav_element SignUp" href="SignUp.html">SignUP</a></li>\
        <li ><a window_id="login_div" class="nav_element Login" onclick="popWindow(this);" href="#">Login</a></li>\
        <li ><a class="nav_element about" href="about.html">About</a></li>\
        </ul>\
        </div>\
        </div>\
        </nav>\
        ';
    }
    else
    {
        obj.innerHTML='<nav class="navbar navbar-custom">\
        <div class="container-fluid">\
        <div class="navbar-header">\
        <a class="navbar-brand" href="index.html">Health Overflow</a>\
        </div>\
        <div>\
        <ul class="nav navbar-nav">\
        <li ><a class="nav_element home" href="index.html">Home</a></li>\
        <li ><a class="nav_element forum" href="http://45.56.85.191:80/index">Forum</a></li>\
        <li ><a class="nav_element health_manager" href="HealthManager.html">Health Manager</a></li>\
        <li ><a class="nav_element SignUp" href="SignUp.html">SignUP</a></li>\
        <li ><a window_id="login_div" class="nav_element Login" onclick="logout();" href="#">Logout</a></li>\
        <li ><a class="nav_element about" href="about.html">About</a></li>\
        <li ><a class="nav_element user_details" href="#"> Hello '+user_details.details[0].username+'</a></li>\
        </ul>\
        </div>\
        </div>\
        </nav>\
        ';
    }


    console.log("in function");
}
//id == login_div
//id == creat_diet_window
//id == error_diet_window
// window id is an attribute of button ,button will be passin and get the window_id to show specific pop up window
//msg is an attribute in tag msg="hhahha" , to insert message of some pop up window e.g. diet error pop up
function popWindow(trigger_obj)
{
    var div_id = trigger_obj.getAttribute("window_id");
    var msg = trigger_obj.getAttribute("msg");
    console.log(""+div_id);
    one_obj=document.getElementById(div_id);
    if(one_obj==null)
    {
        windowPOPup(div_id,msg);
    }
    else
    {
        windowPOPdown(one_obj);
    }
}

function windowPOPup(div_id,msg) {
    one_obj = document.createElement('div');
    one_obj.setAttribute("id", div_id);
    one_obj.setAttribute("class","window");
    if (div_id == "login_div")
    {
        one_obj.innerHTML = "<div class='login_form' >\
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
            <button type='button' class='btn btn-default' onclick='doLogin_popup();'>Login</button>\
            <button type='button' window_id='login_div' onclick='popWindow(this);' class='btn btn-default'>Close</button>\
            </form>\
            ";
    }
    else if(div_id == "creat_diet_window")
    {
        one_obj.innerHTML = "<div>\
            <label>Diet Plan Name </label>\
            <input type='text'/>\
            <br />\
            <label>Diet Plan Type </label>\
            <select>\
            <option value='L'>Loss Weight</option>\
            <option value='F'>SFitnessaab</option>\
            <option value='G'>General</option>\
            </select>\
            <button type='button' window_id='creat_diet_window' onclick='popWindow(this);' class='btn btn-default'>Close</button>\
            </div>";

    }
    else if (div_id=="error_diet_window")
    {
        one_obj.innerHTML = "<div >\
            <p>"+msg+"</p>\
            <br />\
            <button type='button' window_id='error_diet_window' onclick='popWindow(this);' class='btn btn-default'>Close</button>\
            </div>";
    }

    document.body.appendChild(one_obj);
    console.log("in pop up : " + one_obj.innerHTML);
}
function windowPOPdown(one_obj)
{
    one_obj.remove();
    console.log("in pop down");
}

function doLogin_popup()
{
    login_validation(document.getElementById("input_username").value,
        document.getElementById("input_password").value,
        function(){
            checkLoggedIn();
            windowPOPdown(document.getElementById("login_div"));
        });
}

function login_validation(user_name,password,callback)
{
    var xhr_ip = new XMLHttpRequest();
    xhr_ip.open("GET", "http://api.hostip.info/get_json.php?antiCache="+Math.random());
    //xhr.setRequestHeader('Content-Type', 'application/json'); seems not needed!
    // send the collected data as JSON
    xhr_ip.send();

    xhr_ip.onreadystatechange = function() {
        if (xhr_ip.readyState == 4) {

            var xhr = new XMLHttpRequest();
            xhr.open("POST", "http://45.56.85.191:8000/user/login", true);
            var user_data = {};
            user_data["username"] =user_name;
            user_data["password"] =password;
            user_data.userIp = JSON.parse(xhr_ip.responseText).ip;
            //user_data["email"] = "hello@gmail.com";
            if(!user_data_validation(user_data))
            {
                return false;
            }
            xhr.send(JSON.stringify(user_data));
            console.log(JSON.stringify(user_data));

            xhr.onreadystatechange = function() {
                if (xhr.readyState == 4 && xhr.status==200) {

                    var xhr_jonathan_login=new XMLHttpRequest();
                    var user_details_json=JSON.parse(xhr.responseText);
                    if(user_details_json.login=="failed")
                    {
                        document.getElementById("login_error_div").innerHTML="Username or Password wrong X.X";
                        return;
                    }
                    //use userId to logged in
                    console.log("http://45.56.85.191/login_user?user_id="+user_details_json.userId);
                    xhr_jonathan_login.open("GET","http://45.56.85.191/login_user?user_id="+user_details_json.userId,true);
                    xhr_jonathan_login.send();
                    xhr_jonathan_login.onreadystatechange=function(){
                        if (xhr_jonathan_login.readyState == 4 && xhr_jonathan_login.status==200) {
                            console.log("jon " + xhr_jonathan_login.responseText);
                            //insert_navigation_bar(JSON.parse(xhr_jonathan_login.responseText));
                            callback();

                        }
                    }

                }
            }
        }
    };
    //console.log("return_value response body "+return_value);
}



function checkLoggedIn()
{
    var xhr = new XMLHttpRequest();
    var user_detail_json;
    var userId;
    xhr.open("GET","http://45.56.85.191/check_loggedin",true);
    xhr.send();
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status==200) {
            user_detail_json=JSON.parse(xhr.responseText)
            userId=user_detail_json.id;
            //userName = user_detail_json.userName;
            //userWeight = user_detail_json.weight;
            console.log("change navigation "+xhr.responseText);
            insert_navigation_bar(userId,user_detail_json);
        }
    }
}

function logout()
{
    var xhr = new XMLHttpRequest();
    xhr.open("GET","http://45.56.85.191/logout_user");
    xhr.send();
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status==200) {
            checkLoggedIn();
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
checkLoggedIn();
insert_navigation_bar(null,null);

