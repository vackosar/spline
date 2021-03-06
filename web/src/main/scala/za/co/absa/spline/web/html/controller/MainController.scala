/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.web.html.controller

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMethod.{GET, HEAD}
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam, ResponseBody}
import za.co.absa.spline.common.ARMImplicits
import za.co.absa.spline.persistence.api.DataLineageReader
import za.co.absa.spline.web.ExecutionContextImplicit

import scala.concurrent.Future
import scala.io.Source.fromInputStream
import scala.language.postfixOps

@Controller
class MainController @Autowired()
(
  val reader: DataLineageReader
) extends ExecutionContextImplicit {

  @RequestMapping(path = Array("/", "/dataset/**", "/dashboard/**"), method = Array(GET, HEAD))
  def index = "index"

  @RequestMapping(path = Array("/dataset/lineage/_search"), method = Array(GET))
  def lineage(
               @RequestParam("path") path: String,
               @RequestParam("application_id") applicationId: String,
               httpReq: HttpServletRequest,
               httpRes: HttpServletResponse): Future[String] =
    for {
      dsIdOpt <- reader.searchDataset(path, applicationId)
      dsId = dsIdOpt.get // an exception will be translated to 404
    } yield s"redirect:/dataset/$dsId/lineage/overview#datasource"

  @RequestMapping(path = Array("/build-info"), method = Array(GET), produces = Array("text/x-java-properties"))
  @ResponseBody
  def buildInfo: String = {
    import ARMImplicits._
    val lines = for {
      stream <- this.getClass getResourceAsStream "/build.properties"
      line <- fromInputStream(stream).getLines if line.nonEmpty && !line.startsWith("#")
    } yield line
    lines.mkString("\n")
  }
}
