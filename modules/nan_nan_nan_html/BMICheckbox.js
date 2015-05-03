<head>
<script type="text/javascript">

function checkSex(field)
{
  for(var i=0; i < field.length; i++) {
    if(field[i].checked) return field[i].value;
  }
  return false;
}

function checkForm(form)
{
  if(sexValue = checkRadio(form.sexfield)) {
    alert("You selected " + sexValue);
    return true;
  } else {
    alert("Error: No value was selected!");
    return false;
  }
}

</script>
</head>
