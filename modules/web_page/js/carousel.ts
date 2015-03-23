/**
 * Created by liubingfeng on 13/03/15.
 */
function test_image_rotation():void
{
    var imgObj:HTMLImageElement=<HTMLImageElement>document.getElementById("test_image");
    var degree:number;
    if(imgObj.style.transform=="")
    {
        console.log(imgObj.style.transform+"hello world");
        degree=10;
        console.log(imgObj.style.transform = "rotate("+degree+"deg)");

    }
    else
    {
        degree=parseInt(imgObj.style.transform.match(/[0-9]+/)[0]);
        if (degree>=364)
        {
            degree=0;
            imgObj.style.transform = "rotate("+degree+"deg)";
        }
        else
        {
            degree+=1;
            imgObj.style.transform = "rotate(" + degree + "deg)";
        }
        //console.log("in function");
        //console.log(imgObj.style.transform);
        //console.log(imgObj.style.transform.match(/[0-9]+/));
    }

}

function test_image_3d_moving():void
{

}

function get_image_position():void
{
    var obj_collection=document.getElementsByClassName("balloon");
    var i=0;
    console.log("in function");
    console.log(obj_collection.length);
    for (i ;i<obj_collection.length;i++)
    {
        console.log((<HTMLElement>obj_collection[i]).getAttribute("image-position"));
    }
}

function return_image_position_0(obj_collection):number
{
    var i:number,index:number=-1;
    for(i=0;i<obj_collection.length;i++)
    {
        if(parseInt(obj_collection[i].getAttribute("image-position"))==0)
        {
            index=i;
            return index;
        }
    }
    return index
}

function carousel_moving(left_right:string)
{
    var i:number,index:number;
    /*current focused item's index, i.e. middle one*/


}
