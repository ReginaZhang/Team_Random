/**
 * Created by liubingfeng on 13/03/15.
 */
/*function used to determine login window up or not*/
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
    one_obj.innerHTML="<form id='login_form' action='http://45.56.85.191:8000/user/register' method='post' >\
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
    <button type='submit' class='btn btn-default'>Log In</button>\
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

