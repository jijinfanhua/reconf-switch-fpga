package fpga

import chisel3._
import chisel3.util._

class Matcher extends Module {
    val io = IO(new Bundle {
        val pipe        = new Pipeline
        val mod         = new MatcherModify
        val hit         = Output(Bool()) // whether the match hits
        val match_value = Output(UInt(const.MATCH.match_value_width.W))

        val mem         = Flipped(new ClusterReadPort)
    })

    // configuration
    val key_config = Reg(new MatchKeyConfig)
    val table_config = Reg(new MatchTableConfig)
    
    when (io.mod.en) {
        key_config   := io.mod.key_mod
        table_config := io.mod.table_mod
    }

    // pipeline level 1
    // get offset from header_id & internal_offset
    class MatchGetOffset extends Module {
        val io = IO(new Bundle {
            val pipe       = new Pipeline
            val key_config = Input(new MatchKeyConfig)
            val key_offset = Output(UInt(const.PHV.offset_width.W))
        })

        val phv = Reg(new PHV)
        phv := io.pipe.phv_in
        io.pipe.phv_out := phv

        val header_offset = const.PHV.offset(phv.header(io.key_config.header_id))
        val key_offset = header_offset + io.key_config.internal_offset
        
        io.key_offset := key_offset
    }
    
    // pipeline level 2
    // from offset & length get match_key (higher bits)
    class MatchGetKey extends Module {
        val io = IO(new Bundle {
            val pipe       = new Pipeline
            val key_config = Input(new MatchKeyConfig)
            val key_offset = Input(UInt(const.PHV.offset_width.W))
            val match_key  = Output(UInt(const.MATCH.match_key_width.W))
        })

        val phv = Reg(new PHV)
        phv := io.pipe.phv_in
        io.pipe.phv_out := phv

        val key_offset = Reg(UInt(const.PHV.offset_width.W))
        key_offset := io.key_offset

        val match_key_bytes = Wire(Vec(const.MATCH.max_match_key_length, UInt(8.W)))

        // higher bits equals to real match_key, lower bits equals to 0
        for (j <- 0 until const.MATCH.max_match_key_length) {
            val local_offset = j.U(const.MATCH.match_key_length_width.W)
            when (local_offset < io.key_config.key_length) {
                match_key_bytes(const.MATCH.max_match_key_length-j-1) := phv.data(key_offset + local_offset)
            } .otherwise {
                match_key_bytes(const.MATCH.max_match_key_length-j-1) := 0.U(8.W)
            }
        }

        val match_key = match_key_bytes.reduce(Cat(_, _))
        io.match_key := match_key
    }

    // pipeline level 345678 : hash
    
    // pipeline level 9
    // calculate cs
    class MatchGetCs extends Module {
        val io = IO(new Bundle {
            val pipe       = new Pipeline
            val table_config = Input(new MatchTableConfig)
            val key_in     = Input(UInt(const.HASH.hash_key_width.W))
            val key_out    = Output(UInt(const.HASH.hash_key_width.W))
            val addr_in    = Input(UInt(const.SRAM.address_width.W))
            val addr_out   = Output(UInt(const.SRAM.address_width.W))
            val cs_in      = Input(UInt(const.SRAM.sram_id_width.W))
            val cs_out     = Output(UInt(const.SRAM.sram_id_width.W))
            val cs_vec_out = Vec(const.SRAM.sram_number_in_cluster, Output(Bool()))
        })

        val phv = Reg(new PHV)
        phv := io.pipe.phv_in
        io.pipe.phv_out := phv

        val key = Reg(UInt(const.HASH.hash_key_width.W))
        key := io.key_in
        io.key_out := key

        val addr = Reg(UInt(const.SRAM.address_width.W)) // hash value
        addr := io.addr_in
        io.addr_out := addr

        val cs = Reg(UInt(const.SRAM.sram_id_width.W))
        cs := io.cs_in
        io.cs_out := cs

        for (j <- 0 until const.SRAM.sram_number_in_cluster) {
            io.cs_vec_out(j) := false.B
            when (j.U(const.SRAM.sram_id_width.W) === cs) {
                io.cs_vec_out(j) := true.B
            }
            val width_extend = io.table_config.table_width === 2.U(const.SRAM.sram_number_width.W)
            when (width_extend && j.U(const.SRAM.sram_id_width.W) === cs + io.table_config.table_depth) {
                io.cs_vec_out(j) := true.B
            }
        }
    }

    // pipeline level 10
    // read data
    class MatchReadData extends Module {
        val io = IO(new Bundle {
            val pipe       = new Pipeline
            val key_in     = Input(UInt(const.HASH.hash_key_width.W))
            val key_out    = Output(UInt(const.HASH.hash_key_width.W))
            val cs_in      = Input(UInt(const.SRAM.sram_id_width.W))
            val cs_out     = Output(UInt(const.SRAM.sram_id_width.W))
            val addr_in    = Input(UInt(const.SRAM.address_width.W))
            val cs_vec_in  = Vec(const.SRAM.sram_number_in_cluster, Input(Bool()))
            val data_out   = Vec(const.SRAM.sram_number_in_cluster, Output(UInt(const.SRAM.data_width.W)))
            val mem        = Flipped(new ClusterReadPort)
        })

        val phv = Reg(new PHV)
        phv := io.pipe.phv_in
        io.pipe.phv_out := phv

        val key = Reg(UInt(const.HASH.hash_key_width.W))
        key := io.key_in
        io.key_out := key

        val cs = Reg(UInt(const.SRAM.sram_id_width.W))
        cs := io.cs_in
        io.cs_out := cs

        for (j <- 0 until const.SRAM.sram_number_in_cluster) {
            io.mem.cluster(j).en   := io.cs_vec_in(j)
            io.mem.cluster(j).addr := io.addr_in
            io.data_out(j)         := io.mem.cluster(j).data
        }
    }

    // pipeline level 11
    // data reshape
    class MatchDataReshape extends Module {
        val io = IO(new Bundle {
            val pipe        = new Pipeline
            val table_config = Input(new MatchTableConfig)
            val key_in      = Input(UInt(const.HASH.hash_key_width.W))
            val key_out     = Output(UInt(const.HASH.hash_key_width.W))
            val cs_in       = Input(UInt(const.SRAM.sram_id_width.W))
            val data_in     = Vec(const.SRAM.sram_number_in_cluster, Input(UInt(const.SRAM.data_width.W)))
            val data_out    = Output(UInt(const.MATCH.match_data_width.W))
        })
        
        val phv = Reg(new PHV)
        phv := io.pipe.phv_in
        io.pipe.phv_out := phv

        val key = Reg(UInt(const.HASH.hash_key_width.W))
        key := io.key_in
        io.key_out := key

        val data = Reg(UInt(const.MATCH.match_data_width.W))
        val data1 = Wire(UInt((const.MATCH.match_data_width>>1).W))
        val data2 = Wire(UInt((const.MATCH.match_data_width>>1).W))
        
        data1 := 0.U((const.MATCH.match_data_width>>1).W)
        data2 := 0.U((const.MATCH.match_data_width>>1).W)

        for (j <- 0 until const.SRAM.sram_number_in_cluster) {
            when (io.cs_in === j.U(const.SRAM.sram_id_width.W)) {
                data1 := io.data_in(j)
            }
            val width_extend = io.table_config.table_width === 2.U(const.SRAM.sram_number_width.W)
            when (width_extend && io.cs_in + io.table_config.table_depth === j.U(const.SRAM.sram_id_width.W)) {
                data2 := io.data_in(j)
            }
        }

        data := Cat(data1, data2)
        io.data_out := data
    }

    // pipeline level 12
    // get result
    class MatchResult extends Module {
        val io = IO(new Bundle {
            val pipe        = new Pipeline
            val key_config  = Input(new MatchKeyConfig)
            val key_in      = Input(UInt(const.HASH.hash_key_width.W))
            val data_in     = Input(UInt(const.MATCH.match_data_width.W))
            val hit         = Output(Bool())
            val match_value = Output(UInt(const.MATCH.match_value_width.W))
        })

        val phv = Reg(new PHV)
        phv := io.pipe.phv_in
        io.pipe.phv_out := phv

        val key = Reg(UInt(const.HASH.hash_key_width.W))
        key := io.key_in
        val data = Reg(UInt(const.MATCH.match_data_width.W))
        data := io.data_in

        val key_equal = Wire(Vec(const.MATCH.max_match_key_length, Bool()))

        for (j <- 0 until const.MATCH.max_match_key_length) {
            val key_byte = key(const.HASH.hash_key_width-1-j*8, const.HASH.hash_key_width-(j+1)*8)
            val data_byte = data(const.MATCH.match_data_width-1-j*8, const.MATCH.match_data_width-(j+1)*8)
            when (j.U(const.MATCH.match_key_length_width.W) < io.key_config.key_length) {
                key_equal(j) := key_byte === data_byte
            } .otherwise {
                key_equal(j) := true.B
            }
        }

        io.hit := key_equal.reduce(_ && _)
        io.match_value := 0.U(const.MATCH.match_value_width.W)
        for (j <- 1 until (1+const.MATCH.max_match_key_length)) {
            when (j.U(const.MATCH.match_key_length_width.W) === io.key_config.key_length) {
                io.match_value := data(const.MATCH.match_data_width-1-j*8, const.MATCH.match_key_width-j*8)
            }
        }
    }

    // pipeline
    val pipe1 = Module(new MatchGetOffset)
    val pipe2 = Module(new MatchGetKey)
    val pipe3to8 = Module(new Hash)
    val pipe9 = Module(new MatchGetCs)
    val pipe10 = Module(new MatchReadData)
    val pipe11 = Module(new MatchDataReshape)
    val pipe12 = Module(new MatchResult)

    pipe1.io.pipe.phv_in <> io.pipe.phv_in
    pipe1.io.key_config  := key_config

    pipe2.io.pipe.phv_in <> pipe1.io.pipe.phv_out
    pipe2.io.key_offset  <> pipe1.io.key_offset
    pipe2.io.key_config  := key_config

    pipe3to8.io.pipe.phv_in <> pipe2.io.pipe.phv_out
    pipe3to8.io.mod.hash_depth_mod := io.mod.en && io.mod.table_mod.table_depth =/= table_config.table_depth
    pipe3to8.io.mod.hash_depth     := io.mod.table_mod.table_depth
    pipe3to8.io.key_in   <> pipe2.io.match_key

    pipe9.io.pipe.phv_in <> pipe3to8.io.pipe.phv_out
    pipe9.io.key_in      <> pipe3to8.io.key_out
    pipe9.io.addr_in     <> pipe3to8.io.hash_val
    pipe9.io.cs_in       <> pipe3to8.io.hash_val_cs
    pipe9.io.table_config := table_config

    pipe10.io.pipe.phv_in <> pipe9.io.pipe.phv_out
    pipe10.io.key_in      <> pipe9.io.key_out
    pipe10.io.cs_in       <> pipe9.io.cs_out
    pipe10.io.addr_in     <> pipe9.io.addr_out
    pipe10.io.cs_vec_in   <> pipe9.io.cs_vec_out
    pipe10.io.mem         <> io.mem

    pipe11.io.pipe.phv_in <> pipe10.io.pipe.phv_out
    pipe11.io.key_in      <> pipe10.io.key_out
    pipe11.io.cs_in       <> pipe10.io.cs_out
    pipe11.io.data_in     <> pipe10.io.data_out
    pipe11.io.table_config := table_config

    pipe12.io.pipe.phv_in <> pipe11.io.pipe.phv_out
    pipe12.io.key_in      <> pipe11.io.key_out
    pipe12.io.data_in     <> pipe11.io.data_out
    pipe12.io.key_config  := key_config

    io.pipe.phv_out       <> pipe12.io.pipe.phv_out
    io.hit                <> pipe12.io.hit
    io.match_value        <> pipe12.io.match_value
}

object MAT_OBJ extends App {
    Driver.execute(args, () => new Matcher)
}