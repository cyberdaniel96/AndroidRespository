package service;

/**
 * Created by johnn on 10/30/2017.
 */

public class Converter {

    public Converter() {
    }

    public String ToHex(String data){
        StringBuilder sb = new StringBuilder();
        for (char c : data.toCharArray())
            sb.append(Integer.toHexString((int)c));
        return sb.toString();
    }

    public String ToString(String data){
        StringBuilder sb = new StringBuilder();

        for( int i=0; i<data.length()-1; i+=2 ){
            String output = data.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
        }
        return sb.toString();
    }

    public String convertToHex(String[] temp){
        int length = temp.length;
        String result = "";
        for(int i = 0; i < length; i++){
            result = result + ToHex(temp[i])+"/";
        }

        return result.substring(0,result.length() - 1);
    }

    public String[] convertToString(String temp){
        Converter c = new Converter();
        String[] arr = temp.split("/");

        String[] tempResult = new String[arr.length];

        for(int index = 0; index < arr.length; index++){
            tempResult[index] = c.ToString(arr[index]);
            System.out.println(tempResult[index]);
        }

        return tempResult;

    }
}
