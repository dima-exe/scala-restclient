package com.evrone

package object restclient {
  import org.apache.http.impl.client.DefaultHttpClient
  import org.apache.http.impl.conn.PoolingClientConnectionManager
  import org.apache.http.protocol.BasicHttpContext

  class RestClient {
    val httpClient  = new DefaultHttpClient(RestClient.connManager)
    val httpContext = new BasicHttpContext()

    def get   (url: String) = build("GET",    url)
    def post  (url: String) = build("POST",   url)
    def head  (url: String) = build("HEAD",   url)
    def put   (url: String) = build("PUT",    url)
    def patch (url: String) = build("PATCH",  url)
    def delete(url: String) = build("DELETE", url)

    private def build(method: String, url: String): http.RestRequest = {
      http.RestRequest(this, method, url)
    }
  }

  object RestClient {
    lazy val connManager = new PoolingClientConnectionManager()

    def close() = connManager.shutdown()

    def stats = connManager.getTotalStats().toString()
  }

  implicit def request2asBody(req: http.RestRequest) = new {
    import deserializer.RestDeserializer._

    def as[T](implicit deserializer: AbstractDeserializer[T]): Either[String,T] = {
      val httpReq = http.RestBuilder(req.withAccept(deserializer.contentType))
      val maybeHttpRes = http.RestExecutor.getResponse(req.client, httpReq)
      maybeHttpRes.right.flatMap { body =>
        deserializer(body) match {
          case Some(x) => Right(x)
          case None    => Left("Fail to process response")
        }
      }
    }
  }
}

