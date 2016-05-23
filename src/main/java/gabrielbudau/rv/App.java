package gabrielbudau.rv;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Hello world!
 *
 */
public class App {

	private BufferedImage image;
	private List<BufferedImage> randomSubImages;
	private Color finalImg[][];
	private int imgCount;
	private int patchSize;
	private int width;
	private int height;
	private int size;
	private int errorSize;

	App() throws IOException {
		image = ImageIO.read(new File("src\\main\\resources\\img\\brick.jpg"));
		width = image.getWidth();
		height = image.getHeight();
		imgCount = 50;
		patchSize = 50;
		size = 10;
		errorSize = 5;
		int imgSize = (patchSize - patchSize/errorSize)*size + patchSize/errorSize;
		finalImg = new Color[imgSize][];
		for(int i = 0; i < imgSize; i ++){
			finalImg[i] = new Color[imgSize];
			for(int j = 0; j < imgSize; j ++){
				finalImg[i][j] = new Color(0);
			}
		}
		
	}

	private List<BufferedImage> getRandomSubImages(BufferedImage paramImage, int count, int size){
		List<BufferedImage> array = new ArrayList<BufferedImage>();
		
		if(size < paramImage.getHeight() && size < paramImage.getWidth()){
			for(int i = 0; i < count; i ++){
				int startPoint = randInt(0, paramImage.getHeight()-size -1);
				array.add(paramImage.getSubimage(startPoint, startPoint, size, size));
			}
			return array;
		} else {
			return null;
		}
	}
	
	public void saveImages(List<BufferedImage> subImages) throws IOException{
		for(BufferedImage bi:subImages){
			ImageIO.write(bi, "png", new File("" + subImages.indexOf(bi) + ".png"));
		}
	}
	
	public int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	private void composeImg(){
		for (int i = 0; i < this.size; i ++){
			for(int j = 0; j < this.size; j ++){
				addMatrix(i, j);
			}
		}
	}
	
	public void addMatrix(int line, int column){
		int x;
		int y;
		x = column*(this.patchSize - this.patchSize/errorSize);
		y = line*(this.patchSize - this.patchSize/errorSize);
		
		if(line == 0){//first line
			if(column == 0){
				//first column
				insertMatrix(getColorMatrix(randomSubImages.get(randInt(0, randomSubImages.size()-1))), x, y);
				
			} else {
				// calculate left side error
			    Color[][] optimalMatrix = getOptimalOverlapingMatrix(finalImg, x, y);
				insertMatrix(optimalMatrix, x, y);
			    //insertMatrix(getColorMatrix(randomSubImages.get(randInt(0, randomSubImages.size()-1))), x, y);
			}
		} else {
			//calculate left and top error
		    Color[][] optimalMatrix = getOptimal2SideOverlapingMatrix(finalImg, x, y);
		    insertMatrix(optimalMatrix, x, y);
			//insertMatrix(getColorMatrix(randomSubImages.get(randInt(0, randomSubImages.size()-1))), x, y);
		}
		
	}
	
	private Color[][] getOptimalOverlapingMatrix(Color[][] matrix, int x, int y){
	    Color[][] prevOverlap = getSubMatrix(matrix, x, y,  this.patchSize/errorSize, this.patchSize);
	    int min = 0;
	    Color[][] returnMat = null;
	    for(BufferedImage img:this.randomSubImages){
	        Color[][] currentOverMatrix = getColorMatrix(img);
	        Color[][] currentOverColors = getSubMatrix(currentOverMatrix, 0, 0,  this.patchSize/errorSize, this.patchSize);
	        int currentError = getError(prevOverlap, currentOverColors);
	        
	        if(returnMat == null){
	            returnMat = currentOverMatrix;
	        }
	        if (min == 0){
	            min = currentError;
	        }
	        if(currentError < min){
	            min = currentError;
	            returnMat = currentOverMatrix;
	        }
	    }
	    
	    return returnMat;
	}
	
	private Color[][] getOptimal2SideOverlapingMatrix(Color[][] matrix, int x, int y){
        Color[][] prevOverlapSide = getSubMatrix(matrix, x, y, this.patchSize/errorSize, this.patchSize);
        Color[][] prevOverlapTop = getSubMatrix(matrix, x, y, this.patchSize, this.patchSize/errorSize);
        int min = 0;
        Color[][] returnMat = null;
        for(BufferedImage img:this.randomSubImages){
            Color[][] currentOverMatrix = getColorMatrix(img);
            Color[][] currentOverColorsSide = getSubMatrix(currentOverMatrix, 0, 0, this.patchSize/errorSize, this.patchSize);
            Color[][] currentOverColorsTop = getSubMatrix(currentOverMatrix, 0, 0, this.patchSize, this.patchSize/errorSize);
            int currentError = getError(prevOverlapSide, currentOverColorsSide);
            currentError += getError(prevOverlapTop, currentOverColorsTop);
            
            if(returnMat == null){
                returnMat = currentOverMatrix;
            }
            if (min == 0){
                min = currentError;
            }
            if(currentError > min){
                min = currentError;
                returnMat = currentOverMatrix;
            }
        }
        
        return returnMat;
    }
	
	private Color[][] getColorMatrix(BufferedImage img){
		Color[][] colorMatrix = new Color[img.getHeight()][];
		for(int i =0; i < colorMatrix.length; i ++){
			colorMatrix[i] = new Color[img.getWidth()];
		}
		
		for(int i = 0; i < colorMatrix.length; i ++){
			for(int j = 0; j < colorMatrix[0].length; j ++){
				colorMatrix[i][j] = new Color(img.getRGB(i, j));
			}
		}
		return colorMatrix;
	}
	
	private void insertMatrix(Color[][] matrix, int x, int y){
		for (int i = 0; i < matrix.length; i ++){
			for(int j = 0; j < matrix[0].length; j ++){
				
				Color  c = matrix[i][j];
				this.finalImg[x+i][y+j] =  new Color(c.getRed(), c.getGreen(), c.getBlue());
			}
		}
	}
	
	private Color[][] getSubMatrix(Color[][] matrix, int x, int y, int sizeX, int sizeY){
		Color[][] subMatrix = new Color[sizeX][];
		for(int i = 0; i < sizeX; i ++){
			subMatrix[i] = new Color[sizeY];
		}
		
		for(int i = 0; i < sizeX; i ++){
			for(int j = 0; j < sizeY; j ++){
				Color  c = matrix[x+i][y+j];
				subMatrix[i][j] = new Color(c.getRed(), c.getGreen(), c.getBlue());
			}
		}
		
		return subMatrix;
	}
	
	private int getError(Color[][] a, Color[][] b){
		int error = 0;
		for(int i = 0; i < a.length; i ++){
			for(int j = 0; j < a[0].length; j ++){
				error += subtractColors(a[i][j], b[i][j]);
			}
		}
		return error;
	}

	private int subtractColors(Color a,  Color b){
		 return 
				 (Math.abs(a.getRed()-b.getRed())) + 
				 (Math.abs(a.getBlue()-b.getBlue())) + 
				 (Math.abs(a.getGreen()-b.getGreen()));
	}
	
	private BufferedImage createFinalImage(){
		BufferedImage bufferedImage = new BufferedImage(finalImg.length, finalImg[0].length,BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < finalImg.length; x++) {
		    for (int y = 0; y < finalImg[x].length; y++) {
		    	try{
		    		bufferedImage.setRGB(x, y, finalImg[x][y].getRGB());
		    	} catch(NullPointerException npe) {
		    		System.out.println(x + " " + y);
		    	}
		    }
		}
		
		return bufferedImage;
	}
	public static void main(String[] args) throws IOException {
		App app = new App();
		app.randomSubImages = app.getRandomSubImages(app.image, app.imgCount, app.patchSize);
		//app.saveImages(app.randomSubImages);
		app.composeImg();
		ImageIO.write(app.createFinalImage(), "png", new File("final.png"));
	}

}
