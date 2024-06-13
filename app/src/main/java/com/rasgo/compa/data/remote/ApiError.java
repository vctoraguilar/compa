package com.rasgo.compa.data.remote;

import android.util.MalformedJsonException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;

//public class ApiError {
//    public static class ErrorMessage{
//        public String message;
//        public int status;
//
//        public ErrorMessage(String message, int status) {
//            this.message = message;
//            this.status = status;
//        }
//    }
//
//    public static ErrorMessage getErrorFromException(Exception e){
//        return new ErrorMessage(e.getMessage(), e.hashCode());
//    }
//    public static ErrorMessage getErrorFromThrowable(Throwable t){
//        if(t instanceof HttpException){
//            return new ErrorMessage(t.getMessage(),((HttpException)t).code());
//        } else if (t instanceof SocketTimeoutException) {
//            return new ErrorMessage("Tiempo sobrepasado",0);
//        }else if(t instanceof IOException) {
//            if(t instanceof MalformedJsonException){
//                return new ErrorMessage("MalformedJsonException Json from Server",0);
//            } else if (t instanceof ConnectException) {
//                return new ErrorMessage(t.getMessage()+"XAMPP no está corriendo \n Tienes una IP diferente",0);
//            }
//            else{
//                return new ErrorMessage("Sin conexión a Internet",0);
//            }
//        }else{
//            return new ErrorMessage("Error desconocido",0);
//        }
//    }
//}
