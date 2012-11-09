package com.evrone.http.restclient.impl

import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.HttpResponse
import com.twitter.util.Try
import com.evrone.http.RestClient

object HttpRequestExecutor {

  def apply(client: RestClient)(httpReq: HttpRequestBase): Try[HttpResponse] = {
    client.log(httpReq.getRequestLine.toString)

    Try {
      client.httpClient.execute(httpReq, client.httpContext)
    } map { httpRes =>
      client.log(httpRes.getStatusLine.toString)
      httpRes.getStatusLine.getStatusCode match {
        case ok if 200 until 299 contains ok => httpRes
        case _ => throw new UnexpectedHttpResponse(httpRes.getStatusLine.toString)
      }
    }
  }
}

case class UnexpectedHttpResponse(msg: String) extends Exception(msg)
