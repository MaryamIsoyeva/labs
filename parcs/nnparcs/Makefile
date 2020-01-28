all: run

clean:
	rm -f out/MAinMod.jar out/NN.jar

out/MAinMod.jar: out/parcs.jar src/MAinMod.java src/DataToTransf.java
	@javac -cp out/parcs.jar src/MAinMod.java src/DataToTransf.java
	@jar cf out/MAinMod.jar -C src MAinMod.class -C src DataToTransf.class
	@rm -f src/MAinMod.class src/DataToTransf.class

out/NN.jar: out/parcs.jar src/NN.java src/DataToTransf.java
	@javac -cp out/parcs.jar src/NN.java src/DataToTransf.java
	@jar cf out/NN.jar -C src NN.class -C src DataToTransf.class
	@rm -f src/NN.class src/DataToTransf.class

build: out/MAinMod.jar out/NN.jar

run: out/MAinMod.jar out/NN.jar
	@cd out && java -cp 'parcs.jar:MAinMod.jar' MAinMod
