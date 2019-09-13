class GroovyVideoUtils {

    static void explore3DMatrix(double[][][] matrix, String info = "all") {
    
        println "      - Explore matrix - "
        println"-"*30
        if (info == "all" || info == "structure") {
            println "Matrix structure:" 
            println "Rows: ${matrix.size()}"
            println "Cols: ${matrix[0].size()}"
            println "Dimensions: ${matrix[0][0].size()}"
            println"."
            }
        if (info == "all" || info == "values") {
            println "Matrix values: "
            println matrix
            println"."
            }
        if (info == "all" || info == "detail") {
            println "Matrix in detail:"
      
            for (int i = 0; i < matrix.size(); i++) {
                //println(matrix3[i])
                for (int j = 0; j < matrix[i].size(); j++) {
                    //println(matrix3[i][j])
                    for (int z = 0; z < matrix[i][j].size(); z++) {
                         //println(matrix3[i][j][z])
                         double v = matrix[i][j][z]
                         println("matrix[" + i +"][" + j + "][" + z + "] = " + v )
                        }
                    }
                }
            println"."
            }
        println"-"*30    
        }

    static List arrayListFrom3DMatrix(matrix) {
        List resultList = new ArrayList()
        for (int i = 0; i < matrix.size(); i++) {
            //println(matrix3[i])
            for (int j = 0; j < matrix[i].size(); j++) {
                //println(matrix3[i][j])
                for (int z = 0; z < matrix[i][j].size(); z++) {
                     //println(matrix3[i][j][z])
                     double v = matrix[i][j][z]
                     resultList.add(v)
                    }
                }
            }
        
        return resultList
        }

}


def process(arg) {
    listValues = GroovyVideoUtils.arrayListFrom3DMatrix(arg)
    meann = listValues.sum() / listValues.size()
    maxx = listValues.max()
    minn = listValues.min()
    result = "mean="+meann+";min="+minn+";max="+maxx
    return result
}




