<?php
set_error_handler(function($severity, $message, $file, $line) {
    if (error_reporting() & $severity) {
        throw new ErrorException($message, 0, $severity, $file, $line);
    }
});
spl_autoload_register(function ($class_name) 
{
  if ($class_name == "Date")
  {
    return;
  }
  require_once "../src-gen-umple/" . $class_name . '.php';
});

function endsWith($str, $sub ) 
{
  return ( substr( $str, strlen( $str ) - strlen( $sub ) ) == $sub );
}

function loadTestsIn($test,$directoryName)
{
  if ($handle = opendir($directoryName)) { 
    while (false !== ($file = readdir($handle))) 
    { 
      if (endsWith($file,"Test.php")) 
      { 
        $test->addFile("{$directoryName}/{$file}");
      } 
    } 
    closedir($handle); 
  }
}