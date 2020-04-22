// only dependency:

// implementation("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.7.1")
/*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML

fun main() {
    println(StringBuilder().appendHTML().html {
        body {
            div {
                table {
                    tbody {
                        // Each of these tr blocks add 3s of compilation time!
                        // The more elements this table is nested in, the more time it takes for each additional tr element
                        tr {
                            td { +"InChI" }
                            td { +"Foo" }
                        }
                        tr {
                            td { +"InChI" }
                            td { +"Foo" }
                        }
                        tr {
                            td { +"Smiles" }
                            td { +"Bar" }
                        }
                        tr {
                            td { +"ID" }
                            td { +"42" }
                        }
                    }
                }
            }
        }
    }.toString())
}*/