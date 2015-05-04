<head>
<script>

function temp(form)
{
  var f = parseFloat(form.DegF.value, 10);
  var c = 0;
  c = (f - 32.0) * 5.0 / 9.0;
  form.DegC.value = c;
}

</script>
</head>
