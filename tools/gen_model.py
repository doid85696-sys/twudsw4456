import json, uuid, base64, os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
parts = []
def P(name, parent, pivot, cubes, rot=(0, 0, 0)): parts.append((name, parent, pivot, rot, cubes))
BONE = (52, 18)   # большая костяная область текстуры для шипов и клыков

P("body", None, (0, 0, 0), [(-3, -18, -2, 6, 18, 4, 32, 0)])
P("spine", "body", (0, 0, 0), [(-0.5, y, 2, 1, 3, 1, 52, 14) for y in (-17, -14, -11, -8, -5, -2)]
  + [(-1, y, 2, 2, 4, 2) + BONE for y in (-16, -10, -4)])
P("ribs", "body", (0, 0, 0), [(-2.5, y, -3, 5, 1, 1, 52, 8) for y in (-16, -13, -10, -7, -4)])
P("sternum", "body", (0, 0, 0), [(-0.5, -17, -3, 1, 13, 1) + BONE])
P("neck", "body", (0, -18, 0), [(-1, -4, -1, 2, 4, 2, 24, 32)])
P("head", "neck", (0, -4, 0), [(-4, -8, -4, 8, 5, 8, 0, 0)]
  + [(x - 0.5, -3, -4, 1, 2, 1, 56, 14) for x in (-3, -1, 1, 3)]
  + [(-4.5, -7, -5, 9, 1, 1, 32, 22), (-5, -6, -1, 1, 1, 3) + BONE, (4, -6, -1, 1, 1, 3) + BONE])
P("horn_r", "head", (-3, -8, 0), [(-1, -5, -1, 2, 5, 2) + BONE], (0, 0, -0.4))
P("horn_r_tip", "horn_r", (0, -5, 0), [(-0.5, -4, -0.5, 1, 4, 1, 52, 0)], (-0.3, 0, 0.6))
P("horn_l", "head", (3, -8, 0), [(-1, -5, -1, 2, 5, 2) + BONE], (0, 0, 0.4))
P("horn_l_tip", "horn_l", (0, -5, 0), [(-0.5, -4, -0.5, 1, 4, 1, 52, 0)], (-0.3, 0, -0.6))
P("jaw", "head", (0, -3, 3), [(-4, 0, -7, 8, 3, 8, 0, 16)]
  + [(x - 0.5, -2, -7, 1, 2, 1, 56, 14) for x in (-3, -1, 1, 3)]
  + [(-5, -5, -6, 1, 5, 1) + BONE, (4, -5, -6, 1, 5, 1) + BONE])
for side, sx, u in (("right", -1, 0), ("left", 1, 12)):
    P(side + "_arm", "body", (4.5 * sx, -16, 0), [(-1.5, -1, -1.5, 3, 13, 3, u, 32)])
    P(side + "_spike", side + "_arm", (1.5 * sx, -1, 0), [(-1, -5, -1, 2, 5, 2) + BONE], (0, 0, 0.9 * sx))
    P(side + "_fore", side + "_arm", (0, 12, 0), [(-1.5, 0, -1.5, 3, 13, 3, u, 48), (-0.5, -1, 1.5, 1, 1, 3) + BONE])
    P(side + "_hand", side + "_fore", (0, 13, 0), [(x - 0.5, 0, -0.5, 1, 3, 1, 56, 0) for x in (-1, 0, 1)])
    P(side + "_tips", side + "_hand", (0, 3, 0), [(x - 0.5, 0, -0.5, 1, 3, 1, 56, 0) for x in (-1, 0, 1)], (-0.5, 0, 0))
for side, sx, u in (("right", -1, 32), ("left", 1, 48)):
    P(side + "_leg", None, (1.5 * sx, 0, 0), [(-2, 0, -2, 4, 12, 4, u, 32)])
    P(side + "_shin", side + "_leg", (0, 12, 0), [(-1.5, 0, -1.5, 3, 12, 3, u, 47), (-0.5, -1, -4.5, 1, 1, 3) + BONE])
    P(side + "_foot", side + "_shin", (0, 12, 0), [(-2, -1, -5, 4, 1, 6, 32, 25)]
      + [(x, -1, -8, 1, 1, 3) + BONE for x in (-1.5, -0.5, 0.5)] + [(-0.5, -3, 1, 1, 2, 2) + BONE])

# ---------- Java ----------
defs = []
for name, parent, pv, rot, cubes in parts:
    cl = "CubeListBuilder.create()" + "".join(
        ".texOffs(%d, %d).addBox(%sf, %sf, %sf, %sf, %sf, %sf)" % (u, v, x, y, z, w, h, d) for x, y, z, w, h, d, u, v in cubes)
    if rot == (0, 0, 0): pp = "PartPose.offset(%sf, %sf, %sf)" % pv
    else: pp = "PartPose.offsetAndRotation(%sf, %sf, %sf, %sf, %sf, %sf)" % (pv + rot)
    defs.append('        PartDefinition %s = %s.addOrReplaceChild("%s", %s, %s);' % (name, parent or "root", name, cl, pp))

java = '''package com.example.hollowflayer.client;

import com.example.hollowflayer.HollowFlayerMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/** Модель (сгенерирована tools/gen_model.py): шея, рога, рёбра, шипы, зубы, локти, колени, когти. 54 px = 3.4 блока. */
public class FlayerRawModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(HollowFlayerMod.MODID, "flayer"), "main");

    public final ModelPart root, body, neck, head, jaw;
    public final ModelPart rArm, rFore, rHand, lArm, lFore, lHand;
    public final ModelPart rLeg, rShin, lLeg, lShin;

    public FlayerRawModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        neck = body.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        rArm = body.getChild("right_arm");
        rFore = rArm.getChild("right_fore");
        rHand = rFore.getChild("right_hand");
        lArm = body.getChild("left_arm");
        lFore = lArm.getChild("left_fore");
        lHand = lFore.getChild("left_hand");
        rLeg = root.getChild("right_leg");
        rShin = rLeg.getChild("right_shin");
        lLeg = root.getChild("left_leg");
        lShin = lLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
''' + "\n".join(defs) + '''
        return LayerDefinition.create(mesh, 64, 64);
    }
}
'''
open(os.path.join(ROOT, "src/main/java/com/example/hollowflayer/client/FlayerRawModel.java"), "w").write(java)

# ---------- Blockbench ----------
abs_pv = {}
for name, parent, pv, rot, cubes in parts:
    b = abs_pv.get(parent, (0, 0, 0))
    abs_pv[name] = (b[0] + pv[0], b[1] + pv[1], b[2] + pv[2])
els = {}
for name, parent, pv, rot, cubes in parts:
    ap = abs_pv[name]; lst = []
    for x, y, z, w, h, d, u, v in cubes:
        uid = str(uuid.uuid4()); lst.append(uid)
        els_item = {"name": name, "box_uv": True, "rescale": False, "locked": False,
            "from": [ap[0] + x, 24 - (ap[1] + y + h), ap[2] + z], "to": [ap[0] + x + w, 24 - (ap[1] + y), ap[2] + z + d],
            "autouv": 0, "color": 0, "origin": [ap[0], 24 - ap[1], ap[2]], "uv_offset": [u, v],
            "faces": {f: {"uv": [0, 0, 0, 0], "texture": 0} for f in ("north", "east", "south", "west", "up", "down")},
            "type": "cube", "uuid": uid}
        els[uid] = els_item
    els[name] = lst
def group(name):
    ap = abs_pv[name]
    kids = list(els[name]) + [group(n) for n, p, *_ in parts if p == name]
    return {"name": name, "origin": [ap[0], 24 - ap[1], ap[2]], "color": 0, "uuid": str(uuid.uuid4()), "export": True, "isOpen": True, "children": kids}
outliner = [group(n) for n, p, *_ in parts if p is None]
elements = [v for k, v in els.items() if isinstance(v, dict)]
tex = os.path.join(ROOT, "src/main/resources/assets/hollowflayer/textures/entity/flayer_raw.png")
b64 = "data:image/png;base64," + base64.b64encode(open(tex, "rb").read()).decode()
model = {"meta": {"format_version": "4.5", "model_format": "free", "box_uv": True}, "name": "flayer",
    "model_identifier": "flayer", "visible_box": [1, 3, 0], "resolution": {"width": 64, "height": 64},
    "elements": elements, "outliner": outliner,
    "textures": [{"path": "", "name": "flayer_raw.png", "folder": "", "namespace": "", "id": "0", "particle": False,
                  "render_mode": "default", "source": b64, "uuid": str(uuid.uuid4())}]}
os.makedirs("/mnt/user-data/outputs", exist_ok=True)
json.dump(model, open("/mnt/user-data/outputs/flayer.bbmodel", "w"))
print("model ok:", len(elements), "cubes")
